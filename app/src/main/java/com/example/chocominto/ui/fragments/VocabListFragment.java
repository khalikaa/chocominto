package com.example.chocominto.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.chocominto.adapters.VocabListAdapter;
import com.example.chocominto.data.models.Vocab;
import com.example.chocominto.data.repository.VocabRepository;
import com.example.chocominto.databinding.FragmentVocabListBinding;
import com.example.chocominto.ui.activities.VocabDetailActivity;
import com.example.chocominto.utils.AudioHelper;

import java.util.List;

public class VocabListFragment extends Fragment {
    private FragmentVocabListBinding binding;
    private static final String TAG = "VocabListFragment";
    private VocabListAdapter vocabListAdapter;
    private VocabRepository repository;
    private boolean isLoading = false;
    private String nextUrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVocabListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AudioHelper.getInstance().setContext(requireContext());
        Log.d(TAG, "AudioHelper initialized");

        repository = VocabRepository.getInstance();

        vocabListAdapter = new VocabListAdapter();
        vocabListAdapter.setOnItemClickListener(this::navigateToDetailScreen);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(vocabListAdapter);

        // Setup pull-to-refresh
//        binding.swipeRefresh.setOnRefreshListener(() -> loadVocabularyData(true));

        setupPagination(layoutManager);
        loadVocabularyData(false);
    }

    private void setupPagination(LinearLayoutManager layoutManager) {
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && nextUrl != null) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadMoreVocabulary();
                    }
                }
            }
        });
    }

    private void navigateToDetailScreen(Vocab item) {
        Log.d(TAG, "Selected vocabulary: " + item.getCharacter());

        Intent intent = VocabDetailActivity.createIntent(
                requireContext(),
                item.getId(),
                VocabDetailActivity.MODE_NORMAL,
                "vocab_list_fragment"
        );
        startActivity(intent);
    }

    private void loadVocabularyData(boolean forceRefresh) {
        showLoading(true);
        isLoading = true;

        repository.getVocabList(new VocabRepository.VocabListCallback() {
            @Override
            public void onSuccess(List<Vocab> vocabList, String nextPageUrl) {
                if (!isAdded()) return;

                showLoading(false);
                isLoading = false;
//                binding.swipeRefresh.setRefreshing(false);

                vocabListAdapter.setVocabularyList(vocabList);
                nextUrl = nextPageUrl;

                showEmptyView(vocabList.isEmpty());
                Log.d(TAG, "Loaded " + vocabList.size() + " vocabulary items");
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return; // Fragment not attached to activity

                showLoading(false);
                isLoading = false;
//                binding.swipeRefresh.setRefreshing(false);

                showError(message);
                showEmptyView(vocabListAdapter.getItemCount() == 0);
            }
        }, forceRefresh);
    }

    private void loadMoreVocabulary() {
        if (nextUrl != null && !isLoading) {
            isLoading = true;

            repository.getNextVocabPage(nextUrl, new VocabRepository.VocabListCallback() {
                @Override
                public void onSuccess(List<Vocab> vocabList, String nextPageUrl) {
                    if (!isAdded()) return; // Fragment not attached to activity

                    isLoading = false;
                    vocabListAdapter.addVocabs(vocabList);
                    nextUrl = nextPageUrl;

                    Log.d(TAG, "Loaded " + vocabList.size() + " more vocabulary items");
                }

                @Override
                public void onError(String message) {
                    if (!isAdded()) return;

                    isLoading = false;
                    showError(message);
                }
            });
        }
    }

    private void showLoading(boolean isLoading) {
        if (binding != null) {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmptyView(boolean isEmpty) {
        if (binding != null) {
            binding.tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AudioHelper.getInstance().release();
        binding = null;
    }
}