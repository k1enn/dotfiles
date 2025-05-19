package com.example.elsa_speak_clone.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.elsa_speak_clone.database.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {
    private final UserRepository repository;
    private final MutableLiveData<Boolean> loginResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registrationResult = new MutableLiveData<>();

    public UserViewModel(Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public LiveData<Boolean> getLoginResult() {
        return loginResult;
    }

    public LiveData<Boolean> getRegistrationResult() {
        return registrationResult;
    }

    public void loginUser(String usernameOrEmail, String password) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            boolean result = repository.authenticateLocalUser(usernameOrEmail, password);
            loginResult.postValue(result);
        });
    }

    public void loginWithGoogle(String email) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            boolean result = repository.authenticateGoogleUser(email);
            loginResult.postValue(result);
        });
    }

    public void registerUser(String username, String password) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            boolean result = repository.registerLocalUser(username, password);
            registrationResult.postValue(result);
        });
    }

    public void registerWithGoogle(String email) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            boolean result = repository.registerGoogleUser(email);
            registrationResult.postValue(result);
        });
    }
} 