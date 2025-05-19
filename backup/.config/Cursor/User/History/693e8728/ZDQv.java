package com.example.elsa_speak_clone.classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.dao.VocabularyDao;
import com.example.elsa_speak_clone.database.entities.Lesson;
import com.example.elsa_speak_clone.database.entities.Vocabulary;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

// LessonAdapter.java
public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private List<Lesson> lessons;
    private OnLessonClickListener listener;
    private AppDatabase database;
    private VocabularyDao vocabularyDao;

    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson);
    }

    public LessonAdapter(List<Lesson> lessonList, AppDatabase database, OnLessonClickListener listener) {
        this.lessons = lessonList;
        this.database = database;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);
        
        // Set basic lesson information
        holder.tvTitle.setText(lesson.getTopic());
        holder.tvContent.setText(lesson.getLessonContent());
        
        // Get the lesson ID from the current lesson object
        int currentLessonId = lesson.getLessonId();
        
        // Run database operation on a background thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        
        executor.execute(() -> {
            // Retrieve vocabulary count for this specific lesson
            List<String> vocabList = database.vocabularyDao().getWordsByLessonId(currentLessonId);
            int vocabCount = (vocabList != null) ? vocabList.size() : 0;
            
            // Update UI on main thread
            handler.post(() -> {
                holder.tvVocabularyCount.setText("Vocabulary count: " + vocabCount);
            });
        });
        
        // Set click listener for the lesson item
        holder.itemView.setOnClickListener(v -> listener.onLessonClick(lesson));
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvVocabularyCount;

        LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvLessonTitle);
            tvContent = itemView.findViewById(R.id.tvLessonContent);
            tvVocabularyCount = itemView.findViewById(R.id.tvVocabularyCount);
        }
    }
}
