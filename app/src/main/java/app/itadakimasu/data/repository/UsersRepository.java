package app.itadakimasu.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import app.itadakimasu.data.Result;

public class UsersRepository {
    private FirebaseFirestore dbFirestore;
    private AppAuthRepository authRepository;

    public UsersRepository() {
        this.dbFirestore = FirebaseFirestore.getInstance();
        this.authRepository = new AppAuthRepository();
    }

    public void createUser(String email, String username, String password) {
        MutableLiveData<Result<?>> registerResult = authRepository.register(email, username, password);
    }

    public void updateUsername() {

    }

    public void updatePhoto() {

    }


}
