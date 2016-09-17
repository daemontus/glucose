package com.glucose.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.test.rule.ActivityTestRule;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.glucose.app.presenter.LifecycleException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Rule;
import org.junit.Test;

public class PresenterFactoryJavaTest {

    @Rule
    public final ActivityTestRule<EmptyActivity> activityRule = new ActivityTestRule<>(EmptyActivity.class);

    private class MockPresenterHost implements PresenterHost {

        @NotNull
        @Override
        public Activity getActivity() {
            return activityRule.getActivity();
        }

        @NotNull
        @Override
        public PresenterFactory getFactory() {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public Presenter getRoot() {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public <P extends Presenter> P attach(@NotNull Class<P> clazz, @NotNull Bundle arguments, @Nullable ViewGroup parent) {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public <P extends Presenter> P attachWithState(@NotNull Class<P> clazz, @NotNull SparseArray<Bundle> savedState, @NotNull Bundle arguments, @Nullable ViewGroup parent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void detach(@NotNull Presenter presenter) {
            throw new UnsupportedOperationException();
        }
    }

    private final PresenterHost host = new MockPresenterHost();
    private final PresenterFactory factory = new PresenterFactory(host);

    @Test
    public void presenterFactory_recycleActive() {
        /*
        Presenter p = factory.obtain(PresenterFactoryTest.SimplePresenter.class, null);
        //p.performAttach(new Bundle());
        try {
            factory.recycle(p);
            throw new IllegalStateException("Expected LifecycleException");
        } catch (LifecycleException e) {
            //ok
        }
        //p.performDetach();
        factory.recycle(p);
        factory.onDestroy();
        */
    }

}
