package com.giftplanner.ui.auth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.giftplanner.GiftPlannerApplication;
import com.giftplanner.R;
import com.giftplanner.data.repository.AuthRepository;
import com.giftplanner.databinding.FragmentRegisterBinding;
import com.giftplanner.ui.viewmodel.AuthViewModel;
import com.giftplanner.ui.viewmodel.AuthViewModelFactory;

public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        GiftPlannerApplication app = (GiftPlannerApplication) requireActivity().getApplication();
        AuthRepository authRepository = new AuthRepository(
            app.getDatabase().userDao(),
            app.getDatabase().passwordResetOtpDao()
        );
        
        viewModel = new ViewModelProvider(this, new AuthViewModelFactory(authRepository))
            .get(AuthViewModel.class);
        
        setupListeners();
        observeAuthState();
    }
    
    private void setupListeners() {
        binding.btnRegister.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString();
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();
            String confirmPassword = binding.etConfirmPassword.getText().toString();
            
            viewModel.register(username, email, password, confirmPassword);
        });
        
        binding.tvLogin.setOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp()
        );
    }
    
    private void observeAuthState() {
        viewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            String type = state.getType();
            
            if ("LOADING".equals(type)) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnRegister.setEnabled(false);
            } else if ("REGISTER_SUCCESS".equals(type)) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnRegister.setEnabled(true);
                Toast.makeText(requireContext(), R.string.registration_successful, Toast.LENGTH_SHORT).show();
                
                // Navigate back to login after 1.5 seconds
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isAdded()) {
                        Navigation.findNavController(requireView()).navigateUp();
                    }
                }, 1500);
            } else if ("ERROR".equals(type)) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnRegister.setEnabled(true);
                Toast.makeText(requireContext(), state.getMessage(), Toast.LENGTH_SHORT).show();
                viewModel.resetState();
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnRegister.setEnabled(true);
            }
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


