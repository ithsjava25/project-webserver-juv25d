package org.juv25d.di;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContainerTest {

    static class A {}

    static class B {
        final A a;

        public B(A a) {
            this.a = a;
        }
    }

    @Test
    void shouldResolveSimpleDependency() {
        Container container = new Container("org.juv25d");

        B b = container.get(B.class);

        assertNotNull(b);
        assertNotNull(b.a);
    }

    @Test
    void shouldReturnSameInstance() {
        Container container = new Container("org.juv25d");

        A a1 = container.get(A.class);
        A a2 = container.get(A.class);

        assertSame(a1, a2);
    }

    interface Service {}

    static class ServiceImpl implements Service {}

    @Test
    void shouldResolveInterfaceBinding() {
        Container container = new Container("org.juv25d");

        container.bind(Service.class, ServiceImpl.class);

        Service service = container.get(Service.class);

        assertNotNull(service);
        assertTrue(service instanceof ServiceImpl);
    }

    @Test
    void shouldFailForUnboundInterface() {
        Container container = new Container("org.juv25d");

        assertThrows(RuntimeException.class, () ->
            container.get(Service.class)
        );
    }

    static class C1 {
        public C1(C2 c2) {}
    }

    static class C2 {
        public C2(C1 c1) {}
    }

    @Test
    void shouldDetectCircularDependency() {
        Container container = new Container("org.juv25d");

        assertThrows(RuntimeException.class, () ->
            container.get(C1.class)
        );
    }

    @Test
    void shouldUseRegisteredInstance() {
        Container container = new Container("org.juv25d");

        A custom = new A();
        container.register(A.class, custom);

        A resolved = container.get(A.class);

        assertSame(custom, resolved);
    }
}
