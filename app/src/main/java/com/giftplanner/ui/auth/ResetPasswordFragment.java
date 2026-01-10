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
import com.giftplanner.databinding.FragmentResetPasswordBinding;
import com.giftplanner.ui.viewmodel.AuthViewModel;
import com.giftplanner.ui.viewmodel.AuthViewModelFactory;

public class ResetPasswordFragment extends Fragment {
    private FragmentResetPasswordBinding binding;
    private AuthViewModel viewModel;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false);
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
        
        // Use activity scope to get shared ViewModel
        viewModel = new ViewModelProvider(requireActivity(), new AuthViewModelFactory(authRepository))
            .get(AuthViewModel.class);
        
        // Display email
        viewModel.getOtpEmail().observe(getViewLifecycleOwner(), email -> 
            binding.tvEmail.setText("Email: " + email)
        );
        
        setupListeners();
        observeAuthState();
    }
    
    private void setupListeners() {
        binding.btnResetPassword.setOnClickListener(v -> {
            String otp = binding.etOtp.getText().toString();
            String newPassword = binding.etNewPassword.getText().toString();
            String confirmPassword = binding.etConfirmPassword.getText().toString();
            
            viewModel.resetPassword(otp, newPassword, confirmPassword);
        });
    }
    
    private void observeAuthState() {
        viewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            String type = state.getType();
            
            if ("LOADING".equals(type)) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnResetPassword.setEnabled(false);
            } else if ("PASSWORD_RESET_SUCCESS".equals(type)) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnResetPassword.setEnabled(true);
                Toast.makeText(requireContext(), R.string.password_reset_success, Toast.LENGTH_SHORT).show();
                
                // Navigate back to login after 2 seconds
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isAdded()) {
                        Navigation.findNavController(requireView())
                            .navigate(R.id.action_resetPasswordFragment_to_loginFragment);
                    }
                }, 2000);
            } else if ("ERROR".equals(type)) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnResetPassword.setEnabled(true);
                Toast.makeText(requireContext(), state.getMessage(), Toast.LENGTH_SHORT).show();
                viewModel.resetState();
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnResetPassword.setEnabled(true);
            }
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


