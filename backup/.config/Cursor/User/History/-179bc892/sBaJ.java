package com.example.elsa_speak_clone.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.adapters.ChatAdapter;
import com.example.elsa_speak_clone.models.Message;
import com.example.elsa_speak_clone.services.ChatGptService;
import com.example.elsa_speak_clone.utilities.ConfigManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ChatbotActivity extends BaseActivity {
    private static final String TAG = "ChatbotActivity";
    
    private RecyclerView recyclerMessages;
    private EditText editTextMessage;
    private FloatingActionButton buttonSend;
    private ChatAdapter chatAdapter;
    private ChatGptService chatGptService;
    private Handler mainHandler;
    private boolean isProcessing = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);
        
        mainHandler = new Handler(Looper.getMainLooper());
        setupToolbarAndDrawer();

        initializeUI();
        setupChatService();
        showWelcomeMessage();
    }
    
    private void initializeUI() {
        recyclerMessages = findViewById(R.id.recyclerMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter();
        recyclerMessages.setAdapter(chatAdapter);

        buttonSend.setOnClickListener(v -> sendMessage());

        // Set hint in English
        editTextMessage.setHint("Ask assistant anything...");
    }
    
    private void setupChatService() {
        String apiKey = ConfigManager.getOpenAiApiKey();
        if (apiKey.isEmpty()) {
            Toast.makeText(this, "Wrong API key", Toast.LENGTH_LONG).show();
        }
        chatGptService = new ChatGptService(apiKey);
    }
    
    private void showWelcomeMessage() {
        String welcomeMessage = "Xin chào! Tôi là trợ lý học tiếng Anh của bạn. Bạn có thể hỏi tôi về ngữ pháp, từ vựng, hoặc nhờ tôi giúp bạn cải thiện kỹ năng tiếng Anh.";
        chatAdapter.addMessage(new Message(welcomeMessage, Message.TYPE_BOT));
        scrollToBottom();
    }
    
    private void sendMessage() {
        if (isProcessing) {
            Toast.makeText(this, "Đang xử lý tin nhắn trước đó, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String messageText = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }
        
        chatAdapter.addMessage(new Message(messageText, Message.TYPE_USER));
        scrollToBottom();
        
        // Xóa input
        editTextMessage.setText("");
        
        isProcessing = true;

        chatGptService.sendMessage(messageText, new ChatGptService.ChatGptCallback() {
            @Override
            public void onResponse(String response) {
                mainHandler.post(() -> {
                    chatAdapter.addMessage(new Message(response, Message.TYPE_BOT));
                    scrollToBottom();
                    isProcessing = false;
                });
            }
            
            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    chatAdapter.addMessage(new Message(error, Message.TYPE_BOT));
                    scrollToBottom();
                    isProcessing = false;
                });
            }
        });
    }
    
    private void scrollToBottom() {
        recyclerMessages.post(() -> {
            int lastPosition = chatAdapter.getItemCount() - 1;
            if (lastPosition >= 0) {
                recyclerMessages.smoothScrollToPosition(lastPosition);
            }
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 