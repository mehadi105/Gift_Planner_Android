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
import com.giftplanner.databinding.FragmentForgotPasswordBinding;
import com.giftplanner.ui.viewmodel.AuthViewModel;
import com.giftplanner.ui.viewmodel.AuthViewModelFactory;

public class ForgotPasswordFragment extends Fragment {
    private FragmentForgotPasswordBinding binding;
    private AuthViewModel viewModel;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
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
        
        // Use activity scope to share ViewModel with ResetPasswordFragment
        viewModel = new ViewModelProvider(requireActivity(), new AuthViewModelFactory(authRepository))
            .get(AuthViewModel.class);
        
        setupListeners();
        observeAuthState();
    }
    
    private void setupListeners() {
        binding.btnSendOtp.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString();
            viewModel.sendOtp(email);
        });
        
        binding.tvBackToLogin.setOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp()
        );
    }
    
    private void observeAuthState() {
        viewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            String type = state.getType();
            
            if ("LOADING".equals(type)) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnSendOtp.setEnabled(false);
            } else if ("OTP_SENT".equals(type)) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSendOtp.setEnabled(true);
                Toast.makeText(requireContext(), R.string.otp_sent, Toast.LENGTH_LONG).show();
                
                // Navigate to reset password after 2 seconds
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isAdded()) {
                        Navigation.findNavController(requireView())
                            .navigate(R.id.action_forgotPasswordFragment_to_resetPasswordFragment);
                    }
                }, 2000);
            } else if ("ERROR".equals(type)) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSendOtp.setEnabled(true);
                Toast.makeText(requireContext(), state.getMessage(), Toast.LENGTH_SHORT).show();
                viewModel.resetState();
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSendOtp.setEnabled(true);
            }
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


