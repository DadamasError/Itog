    package com.example.itog.ui.gestures;

    import android.os.Bundle;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.fragment.app.Fragment;
    import androidx.lifecycle.ViewModelProvider;

    import com.example.itog.databinding.FragmentGesturesBinding;

    public class GesturesFragment extends Fragment {

        private FragmentGesturesBinding binding;

        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            GesturesViewModel gesturesViewModel =
                    new ViewModelProvider(this).get(GesturesViewModel.class);

            binding = FragmentGesturesBinding.inflate(inflater, container, false);
            View root = binding.getRoot();

            final TextView textView = binding.textGestures;
            gesturesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
            return root;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            binding = null;
        }
    }