package com.example.chocominto.ui.activities;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.chocominto.R;
import com.example.chocominto.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.choco));
        }
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            NavigationUI.setupWithNavController(binding.bottomNav, navController);

            if (getSupportActionBar() != null) {
                AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                        R.id.learnFragment, R.id.reviewFragment, R.id.vocabListFragment)
                        .build();
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            }

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                updateActionBarTitle(destination);
            });
        }
    }

    private void updateActionBarTitle(NavDestination destination) {
        if (getSupportActionBar() == null) return;

        int id = destination.getId();

        if (id == R.id.learnFragment) {
            getSupportActionBar().setTitle("Learn New Words");
        } else if (id == R.id.reviewFragment) {
            getSupportActionBar().setTitle("Review Words");
        } else if (id == R.id.vocabListFragment) {
            getSupportActionBar().setTitle("Vocabulary List");
        } else {
            CharSequence label = destination.getLabel();
            if (label != null) {
                getSupportActionBar().setTitle(label);
            }
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        return (navController != null && navController.navigateUp())
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}