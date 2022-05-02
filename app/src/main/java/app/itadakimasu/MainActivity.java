package app.itadakimasu;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Arrays;
import java.util.List;

import app.itadakimasu.databinding.ActivityMainBinding;
import app.itadakimasu.ui.SelectMediaDialogFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // Create a list with fragments that doesn't have the BottomNavigation.
        List<Integer> fragmentsWithNoNavigator = Arrays.asList(
                R.id.navigation_login,
                R.id.navigation_register,
                R.id.navigation_photo_addition);

        // Adds a listener, when the navigation is triggered it'll check the destination's fragment.
        // If the fragment is one from the list, the BottomNavigation will be hiden; if not, it will be visible.
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController navController, @NonNull NavDestination navDestination, @Nullable Bundle bundle) {
                if (fragmentsWithNoNavigator.contains(navDestination.getId())) {
                    hideNavView();
                } else {
                    showNavView();
                }
            }
        });

        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    private void hideNavView() {
        binding.navView.setVisibility(View.GONE);
    }

    private void showNavView() {
        binding.navView.setVisibility(View.VISIBLE);
    }

}