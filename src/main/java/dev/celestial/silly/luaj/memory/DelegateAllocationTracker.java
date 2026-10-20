package dev.celestial.silly.luaj.memory;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class DelegateAllocationTracker<WrappedErr extends Throwable, E extends Throwable> implements AllocationTracker<E> {
    private final AllocationTracker<WrappedErr> delegate;
    private final Class<WrappedErr> reifiedWrappedErr;
    private final Function<WrappedErr, E> wrapper;

    public DelegateAllocationTracker(
            @NotNull AllocationTracker<WrappedErr> delegate,
            @NotNull Class<WrappedErr> reifiedWrappedErr,
            @NotNull Function<WrappedErr, E> wrapper
    ) {
        this.delegate = delegate;
        this.reifiedWrappedErr = reifiedWrappedErr;
        this.wrapper = wrapper;
    }

    @Override
    public long getTotal() {
        return delegate.getTotal();
    }

    @Override
    public Map<String, Integer> findCulprits() {
        return delegate.findCulprits();
    }

    @Override
    public void track(byte @NotNull [] byteArray) throws E {
        try {
            delegate.track(byteArray);
        } catch (Throwable thrown) {
            if (reifiedWrappedErr.isInstance(thrown))
                throw wrapper.apply((WrappedErr) thrown);
            throw (E) thrown;
        }
    }

    @Override
    public void track(int @NotNull [] intArray) throws E {
        try {
            delegate.track(intArray);
        } catch (Throwable thrown) {
            if (reifiedWrappedErr.isInstance(thrown))
                throw wrapper.apply((WrappedErr) thrown);
            throw (E) thrown;
        }
    }

    @Override
    public void track(float @NotNull [] floatArray) throws E {
        try {
            delegate.track(floatArray);
        } catch (Throwable thrown) {
            if (reifiedWrappedErr.isInstance(thrown))
                throw wrapper.apply((WrappedErr) thrown);
            throw (E) thrown;
        }
    }

    @Override
    public void track(Object @NotNull [] objectArray) throws E {
        try {
            delegate.track(objectArray);
        } catch (Throwable thrown) {
            if (reifiedWrappedErr.isInstance(thrown))
                throw wrapper.apply((WrappedErr) thrown);
            throw (E) thrown;
        }
    }

    @Override
    public void track(@NotNull String string) throws E {
        try {
            delegate.track(string);
        } catch (Throwable thrown) {
            if (reifiedWrappedErr.isInstance(thrown))
                throw wrapper.apply((WrappedErr) thrown);
            throw (E) thrown;
        }
    }

    @Override
    public State<E> track(@NotNull Object obj, int amount) throws E {
        try {
            return new DelegateState(delegate.track(obj, amount));
        } catch (Throwable thrown) {
            if (reifiedWrappedErr.isInstance(thrown))
                throw wrapper.apply((WrappedErr) thrown);
            throw (E) thrown;
        }
    }

    private class DelegateState implements AllocationTracker.State<E> {

        private final AllocationTracker.State<WrappedErr> delegate;
        public DelegateState(AllocationTracker.State<WrappedErr> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void changeSize(int change) throws E {
            try {
                delegate.changeSize(change);
            } catch (Throwable thrown) {
                if (reifiedWrappedErr.isInstance(thrown))
                    throw wrapper.apply((WrappedErr) thrown);
                throw (E) thrown;
            }
        }

        @Override
        public void run() {
            delegate.run();
        }
    }
}
