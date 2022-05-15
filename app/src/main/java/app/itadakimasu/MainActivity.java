package app.itadakimasu;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import app.itadakimasu.databinding.ActivityMainBinding;

/**
 * Activity of the application, charges the nav controller, the BottomNavigation visibility on fragments
 * and the implementation of an AuthStateListener.
 */
public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController = Objects.requireNonNull(navHostFragment).getNavController();

        // Create a list with fragments that doesn't have the BottomNavigation.
        List<Integer> fragmentsWithNoNavigator = Arrays.asList(
                R.id.navigation_login,
                R.id.navigation_register,
                R.id.navigation_photo_addition,
                R.id.navigation_recipe_creation,
                R.id.navigation_ingredient_creation,
                R.id.navigation_step_Creation);

        // Adds a listener, when the navigation is triggered it'll check the destination's fragment.
        // If the fragment is one from the list, the BottomNavigation will be hidden; if not, it will be visible.
        navController.addOnDestinationChangedListener((navController1, navDestination, bundle) -> {
            if (fragmentsWithNoNavigator.contains(navDestination.getId())) {
                hideNavView();
            } else {
                showNavView();
            }
        });


        // Set ups the navigation controller with the bottom navigation view,
        // so the user can navigate between fragments using this view.
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    /**
     * Sets the bottom navigation view visibility as gone. (Hides it).
     */
    private void hideNavView() {
        binding.navView.setVisibility(View.GONE);
    }

    /**
     * Sets the bottom navigation view visibility as visible.
     */
    private void showNavView() {
        binding.navView.setVisibility(View.VISIBLE);
    }

    /**
     * When the configuration lifecycle reach to start, an instance of authStateListener is created and setted.
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    /**
     * When the configuration lifecycle reach onStop, the authStateListener is removed.
     */
    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    /**
     * Implements auth state listener, when the user authentication expires (ex: Sign out), they are sent to the login.
     * The Navigation's back stack is cleared so the user won't be able to return to the app unless they authenticate.
     * @param firebaseAuth - the Firebase authentication class that will let the program to get the authentication data.
     */
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() == null) {
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
            NavController navController = Objects.requireNonNull(navHostFragment).getNavController();
            navController.navigate(R.id.auth_navigation, null, new NavOptions.Builder().setPopUpTo(R.id.navigation_home, true).build());
        }
    }
}