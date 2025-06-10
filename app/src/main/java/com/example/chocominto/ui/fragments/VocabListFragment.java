package com.example.chocominto.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chocominto.adapters.VocabListAdapter;
import com.example.chocominto.data.models.Vocab;
import com.example.chocominto.data.repository.VocabRepository;
import com.example.chocominto.databinding.FragmentVocabListBinding;
import com.example.chocominto.ui.activities.VocabDetailActivity;
import com.example.chocominto.utils.AudioHelper;

import java.util.ArrayList;
import java.util.List;

public class VocabListFragment extends Fragment {
    private FragmentVocabListBinding binding;
    private static final String TAG = "VocabListFragment";
    private VocabListAdapter vocabListAdapter;
    private VocabRepository repository;
    private boolean isLoading = false;
    private String nextUrl;
    private String currentQuery = "";
    private List<Vocab> allVocabList = new ArrayList<>();

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

        setupPagination(layoutManager);
        setupSearchView();
        loadVocabularyData(false);
    }


    private void setupSearchView() {
        binding.searchView.setIconifiedByDefault(false);
        binding.searchView.setQueryHint("Search vocabulary...");

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                filterVocabs();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                filterVocabs();
                return true;
            }
        });

        binding.searchView.setOnCloseListener(() -> {
            currentQuery = "";
            filterVocabs();
            return false;
        });

        binding.searchView.setOnClickListener(v -> {
            binding.searchView.onActionViewExpanded();
            binding.searchView.requestFocus();
        });
    }

    private void filterVocabs() {
        if (allVocabList.isEmpty()) {
            return;
        }

        List<Vocab> filteredList = new ArrayList<>();

        if (currentQuery == null || currentQuery.isEmpty()) {
            filteredList.addAll(allVocabList);
        } else {
            String lowerQuery = currentQuery.toLowerCase();
            for (Vocab vocab : allVocabList) {
                if (vocab.getCharacter().toLowerCase().contains(lowerQuery) ||
                        vocab.getMeaning().toLowerCase().contains(lowerQuery) ||
                        vocab.getPartOfSpeech().toLowerCase().contains(lowerQuery) ||
                        vocab.getReading().toLowerCase().contains(lowerQuery)) {

                    filteredList.add(vocab);
                }
            }
        }

        vocabListAdapter.setVocabularyList(filteredList);

        boolean isEmpty = filteredList.isEmpty();
        showEmptyView(isEmpty);

        if (isEmpty && currentQuery != null && !currentQuery.isEmpty()) {
            binding.tvEmpty.setText("No results found for \"" + currentQuery + "\"");
        } else if (isEmpty) {
            binding.tvEmpty.setText("No vocabulary found");
        }
    }

    private void setupPagination(LinearLayoutManager layoutManager) {
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (currentQuery == null || currentQuery.isEmpty()) {
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

                allVocabList.clear();
                allVocabList.addAll(vocabList);

                if (currentQuery != null && !currentQuery.isEmpty()) {
                    filterVocabs();
                } else {
                    vocabListAdapter.setVocabularyList(vocabList);
                    showEmptyView(vocabList.isEmpty());
                }

                nextUrl = nextPageUrl;
                Log.d(TAG, "Loaded " + vocabList.size() + " vocabulary items");
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return; // Fragment not attached to activity

                showLoading(false);
                isLoading = false;

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

                    allVocabList.addAll(vocabList);

                    if (currentQuery != null && !currentQuery.isEmpty()) {
                        filterVocabs();
                    } else {
                        vocabListAdapter.addVocabs(vocabList);
                    }

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
            binding.searchView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
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