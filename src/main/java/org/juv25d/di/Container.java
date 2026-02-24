package org.juv25d.di;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A lightweight Dependency Injection (DI) container.
 *
 *This container supports:
 *     Manual bean registration
 *     Interface-to-implementation bindings
 *     Constructor-based dependency injection
 *     Automatic instantiation of classes within a base package
 *     Circular dependency detection
 *
 * The container resolves dependencies recursively by inspecting constructor
 * parameters and creating required objects on demand.
 */
public class Container {

    /**
     * Stores fully constructed singleton instances (beans).
     */
    private final Map<Class<?>, Object> beans = new ConcurrentHashMap<>();

    /**
     * Maps abstractions (interfaces) to concrete implementations.
     */
    private final Map<Class<?>, Class<?>> bindings = new ConcurrentHashMap<>();

    /**
     * Tracks currently resolving types per thread to detect circular dependencies.
     */
    private final ThreadLocal<Set<Class<?>>> resolving =
        ThreadLocal.withInitial(HashSet::new);

    /**
     * Per-type locks to prevent concurrent creation of the same bean
     */
    private final ConcurrentHashMap<Class<?>, ReentrantLock> locks = new ConcurrentHashMap<>();

    /**
     * Base package used to restrict which classes can be auto-instantiated.
     */
    private final String basePackage;

    /**
     * Creates a new DI container.
     *
     * @param basePackage the base package where classes are allowed
     *                    to be auto-created by the container
     */
    public Container(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * Registers a pre-created instance as a bean.
     *
     * This is typically used for:
     *
     * Configuration objects
     * External dependencies
     * Manually constructed services
     *
     * @param type     the class type
     * @param instance the instance to register
     * @param <T>      the type of the bean
     */
    public <T> void register(Class<T> type, T instance) {
        beans.put(type, instance);
    }

    /**
     * Binds an interface (or abstraction) to a concrete implementation.
     *
     * When the abstraction is requested, the container will resolve
     * the specified implementation instead.
     *
     * @param abstraction    the interface or abstract class
     * @param implementation the concrete implementation class
     * @param <T>            the type being bound
     */
    public <T> void bind(Class<T> abstraction, Class<? extends T> implementation) {
        bindings.put(abstraction, implementation);
    }

    /**
     * Retrieves or creates an instance of the given type.
     *
     * @param type the class to resolve
     * @param <T>  the type
     * @return a fully constructed instance
     */
    public <T> T get(Class<T> type) {
        return resolve(type);
    }

    /**
     * Creates a new instance of a class using constructor injection.
     *
     * The constructor is selected based on:
     *
     * {@code @Inject} annotation (highest priority)
     * Largest resolvable constructor<
     *
     * @param clazz the class to instantiate
     * @return a new instance
     */
    public Object create(Class<?> clazz) {
        try {
            Constructor<?> constructor = findBestConstructor(clazz);

            Object[] args = Arrays.stream(constructor.getParameterTypes())
                .map(this::resolve)
                .toArray();

            constructor.setAccessible(true);
            return constructor.newInstance(args);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    /**
     * Resolves a type into a fully constructed instance.
     *
     * This method:
     *
     * Returns existing beans if available.
     * Resolves interface bindings.
     * Detects circular dependencies per thread.
     * Automatically creates instances for constructable classes in the base package.
     * Uses a per-type ReentrantLock with timeout to prevent deadlocks across threads.
     *
     * @param type the class to resolve
     * @param <T>  the type of the bean
     * @return a fully constructed instance
     * @throws RuntimeException if the type is unbound, non-constructable, part of a circular dependency,
     *                          deadlock is detected, or the thread is interrupted while waiting for the lock
     */
    @SuppressWarnings("unchecked")
    private <T> T resolve(Class<T> type) {
        // 0. Resolve interface bindings
        if (bindings.containsKey(type)) {
            type = (Class<T>) bindings.get(type);
        }

        // 1. Cannot resolve unbound interface
        if (type.isInterface() && !bindings.containsKey(type)) {
            throw new RuntimeException("No binding found for interface: " + type.getName());
        }

        // 2. Fast path: return existing bean
        Object existing = beans.get(type);
        if (existing != null) return (T) existing;

        // 3. Acquire per-type lock to avoid concurrent creation
        ReentrantLock lock = locks.computeIfAbsent(type, k -> new ReentrantLock());
        boolean acquired = false;
        try {
            acquired = lock.tryLock(5, java.util.concurrent.TimeUnit.SECONDS);
            if (!acquired) {
                throw new RuntimeException(
                    "Possible deadlock detected while resolving: " + type.getName() +
                        "\nCurrent resolution path: " + resolving.get()
                );
            }

            // Double-check after acquiring lock
            existing = beans.get(type);
            if (existing != null) return (T) existing;

            // 4. Circular dependency detection
            Set<Class<?>> current = resolving.get();
            if (!current.add(type)) {
                throw new RuntimeException("Circular dependency detected: " + current);
            }

            try {
                // 5. Auto-create if inside basePackage
                if (type.getPackageName().startsWith(basePackage)) {
                    if (!isConstructable(type)) {
                        throw new RuntimeException("Cannot construct: " + type.getName());
                    }
                    T instance = (T) create(type);
                    beans.put(type, instance);
                    return instance;
                }

                throw new RuntimeException(
                    "No bean registered for type: " + type.getName() +
                        "\nResolution path: " + current
                );
            } finally {
                current.remove(type);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while resolving: " + type.getName(), e);
        } finally {
            if (acquired) {
                lock.unlock();
            }
        }
    }

    /**
     * Selects the most appropriate constructor for instantiation.
     *
     * Selection rules:
     *
     * If exactly one constructor is annotated with {@code @Inject}, use it
     * Otherwise, choose the largest constructor whose parameters can all be resolved
     *
     * @param filterClass the target class
     * @return selected constructor
     */
    private Constructor<?> findBestConstructor(Class<?> filterClass) {
        Constructor<?>[] constructors = filterClass.getDeclaredConstructors();

        // Prefer @Inject
        List<Constructor<?>> injectCtors = Arrays.stream(constructors)
            .filter(c -> c.isAnnotationPresent(Inject.class))
            .toList();

        if (injectCtors.size() > 1) {
            throw new RuntimeException("Multiple @Inject constructors in " + filterClass.getName());
        }

        if (injectCtors.size() == 1) {
            return injectCtors.get(0);
        }

        // Fallback: largest resolvable constructor
        return Arrays.stream(constructors)
            .sorted((c1, c2) -> Integer.compare(c2.getParameterCount(), c1.getParameterCount()))
            .filter(this::canResolveAll)
            .findFirst()
            .orElseThrow(() ->
                new RuntimeException("No suitable constructor found for " + filterClass.getName())
            );
    }

    /**
     * Checks whether all constructor parameters can be resolved without
     * actually creating instances. This method is used during constructor
     * selection to avoid premature bean instantiation and to detect circular
     * dependencies using the shared resolving set.
     *
     * @param ctor the constructor to test
     * @return {@code true} if all parameters are resolvable without instantiation,
     * {@code false} if any parameter is unresolvable or part of a cycle
     */
    private boolean canResolveAll(Constructor<?> ctor) {
        Set<Class<?>> current = resolving.get();
        for (Class<?> param : ctor.getParameterTypes()) {
            if (!canResolve(param, current)) return false;
        }
        return true;
    }

    /**
     * Determines whether a type can be resolved without creating or caching
     * an instance. This dry-run method is used during constructor selection
     * and leverages the shared resolving set for circular dependency detection.
     *
     * Resolution rules:
     *
     * Interface bindings are resolved to their implementation.
     * Already registered beans are trivially resolvable.
     * Classes inside the base package that are instantiable are recursively
     * checked for resolvable constructor parameters.
     * If the type is already in the resolving set, a circular dependency
     * is detected and the method returns {@code false}.
     *
     *
     * @param type    the class type to check
     * @param current the set of types currently being resolved to detect cycles
     * @return {@code true} if the type can be resolved without instantiation,
     * {@code false} otherwise
     */
    private boolean canResolve(Class<?> type, Set<Class<?>> current) {
        if (bindings.containsKey(type)) {
            type = (Class<?>) bindings.get(type);
        }

        if (type.isInterface() && !bindings.containsKey(type)) {
            return false;
        }

        if (!current.add(type)) {
            return false;
        }

        try {
            if (beans.containsKey(type)) return true;

            if (type.getPackageName().startsWith(basePackage) && isConstructable(type)) {
                try {
                    Constructor<?> constructor = findBestConstructor(type);
                    for (Class<?> param : constructor.getParameterTypes()) {
                        if (!canResolve(param, current)) return false;
                    }
                } catch (RuntimeException e) {
                    return false;
                }
                return true;
            }

            return false;

        } finally {
            current.remove(type);
        }
    }

    /**
     * Determines whether a class is instantiable.
     *
     * A class is considered constructable if it is not an interface and not abstract.
     *
     * @param filterClass the class to check
     * @return {@code true} if the class can be instantiated, {@code false} otherwise
     */
    private boolean isConstructable(Class<?> filterClass) {
        return !filterClass.isInterface() && !Modifier.isAbstract(filterClass.getModifiers());
    }
}
