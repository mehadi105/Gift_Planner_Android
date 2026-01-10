package com.giftplanner.ui.auth;

import android.os.Bundle;
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
import com.giftplanner.databinding.FragmentLoginBinding;
import com.giftplanner.ui.viewmodel.AuthViewModel;
import com.giftplanner.ui.viewmodel.AuthViewModelFactory;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;
    private GiftPlannerApplication app;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        app = (GiftPlannerApplication) requireActivity().getApplication();
        AuthRepository authRepository = new AuthRepository(
            app.getDatabase().userDao()
        );
        
        viewModel = new ViewModelProvider(this, new AuthViewModelFactory(authRepository))
            .get(AuthViewModel.class);
        
        setupListeners();
        observeAuthState();
    }
    
    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString();
            String password = binding.etPassword.getText().toString();
            viewModel.login(username, password);
        });
        
        binding.tvRegister.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment)
        );
    }
    
    private void observeAuthState() {
        viewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            String type = state.getType();
            
            if ("LOADING".equals(type)) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnLogin.setEnabled(false);
            } else if ("LOGIN_SUCCESS".equals(type)) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                
                // Save session
                app.getSessionManager().saveUserId(state.getUserId());
                Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to dashboard when implemented
            } else if ("ERROR".equals(type)) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                Toast.makeText(requireContext(), state.getMessage(), Toast.LENGTH_SHORT).show();
                viewModel.resetState();
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
            }
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


