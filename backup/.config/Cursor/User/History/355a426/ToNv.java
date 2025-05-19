package com.example.elsa_speak_clone.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.elsa_speak_clone.database.entities.Lesson;
import com.example.elsa_speak_clone.database.entities.Quiz;
import com.example.elsa_speak_clone.database.entities.Vocabulary;

import java.util.ArrayList;
import java.util.List;

// To insert default data
public class DataInitializer {
    private static final String TAG = "DataInitializer";
    private static final int CURRENT_DATA_VERSION = 1; // Increment when change
    private static Context context;

    public static void initialize(Context appContext) {
        context = appContext.getApplicationContext(); // Use application context to avoid leaks
    }

    public static void populateDatabase(AppDatabase db) {
        try {
            // Check if context is initialized
            if (context == null) {
                Log.e(TAG, "Context is null. Call DataInitializer.initialize() first");
                // Fall back to direct initialization without version checking
                insertDefaultLessons(db);
                insertDefaultVocabulary(db);
                insertDefaultQuizzes(db);
                return;
            }

            SharedPreferences prefs = context.getSharedPreferences("database_prefs", Context.MODE_PRIVATE);
            int dataVersion = prefs.getInt("DATA_VERSION", 0);
            
            if (dataVersion < CURRENT_DATA_VERSION) {
                // Update data
                updateData(db);
                
                // Save new version
                prefs.edit().putInt("DATA_VERSION", CURRENT_DATA_VERSION).apply();
            } else {
                // First-time initialization
                insertDefaultLessons(db);
                insertDefaultVocabulary(db);
                insertDefaultQuizzes(db);
            }
            
            Log.d(TAG, "Database initialized with version " + CURRENT_DATA_VERSION);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database: " + e.getMessage(), e);
        }
    }

    private static void updateData(AppDatabase db) {
        try {
            Log.d(TAG, "Updating database data to version " + CURRENT_DATA_VERSION);
            

            db.runInTransaction(() -> {
                try {
                    // Delete existing data in reverse order of dependencies
                    db.quizDao().deleteAll();
                    db.vocabularyDao().deleteAll();
                    db.lessonDao().deleteAll();
                } catch (Exception e) {
                   Log.d(TAG, "Can not delete old database", e) ;
                   return;
                }

                // Re-insert all data
                insertDefaultLessons(db);
                insertDefaultVocabulary(db);
                insertDefaultQuizzes(db);
            });
            
            Log.d(TAG, "Database data updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error updating database data: " + e.getMessage(), e);
        }
    }

    private static void insertDefaultLessons(AppDatabase db) {
        List<Lesson> lessons = new ArrayList<>();
        
        // Beginner level (difficulty 1)
        lessons.add(new Lesson(1, "Basic Greetings", "Learn common greeting phrases for daily use.", 1));
        lessons.add(new Lesson(2, "Family Members", "Vocabulary related to family relationships.", 1));
        lessons.add(new Lesson(3, "Food & Dining", "Essential words for ordering and discussing food.", 1));
        
        // Intermediate level (difficulty 2)
        lessons.add(new Lesson(4, "Travel Essentials", "Common phrases and words for travelers.", 2));
        lessons.add(new Lesson(5, "Shopping", "Vocabulary for shopping and transactions.", 2));
        lessons.add(new Lesson(6, "Weather & Seasons", "Terms for describing weather conditions and seasons.", 2));
        
        // Advanced level (difficulty 3)
        lessons.add(new Lesson(7, "Business Communication", "Professional vocabulary for workplace settings.", 3));
        lessons.add(new Lesson(8, "Academic Language", "Terms used in educational and research contexts.", 3));
        lessons.add(new Lesson(9, "Technology & Internet", "Modern vocabulary for digital communication.", 3));
        
        db.lessonDao().insertAll(lessons);
    }

    private static void insertDefaultVocabulary(AppDatabase db) {
        List<Vocabulary> vocabularyList = new ArrayList<>();
        
        // Lesson 1: Basic Greetings
        vocabularyList.add(new Vocabulary(1, "Hello", "/həˈloʊ/", 1));
        vocabularyList.add(new Vocabulary(2, "Goodbye", "/ɡʊdˈbaɪ/", 1));
        vocabularyList.add(new Vocabulary(3, "Thank you", "/θæŋk juː/", 1));
        vocabularyList.add(new Vocabulary(4, "Please", "/pliːz/", 1));
        vocabularyList.add(new Vocabulary(5, "Good morning", "/ɡʊd ˈmɔːrnɪŋ/", 1));
        vocabularyList.add(new Vocabulary(6, "Good evening", "/ɡʊd ˈiːvnɪŋ/", 1));
        vocabularyList.add(new Vocabulary(7, "How are you", "/haʊ ɑːr juː/", 1));
        vocabularyList.add(new Vocabulary(8, "Nice to meet you", "/naɪs tuː miːt juː/", 1));
        vocabularyList.add(new Vocabulary(9, "Excuse me", "/ɪkˈskjuːz miː/", 1));
        vocabularyList.add(new Vocabulary(10, "Sorry", "/ˈsɒri/", 1));

        // Lesson 2: Family Members
        vocabularyList.add(new Vocabulary(11, "Mother", "/ˈmʌðər/", 2));
        vocabularyList.add(new Vocabulary(12, "Father", "/ˈfɑːðər/", 2));
        vocabularyList.add(new Vocabulary(13, "Sister", "/ˈsɪstər/", 2));
        vocabularyList.add(new Vocabulary(14, "Brother", "/ˈbrʌðər/", 2));
        vocabularyList.add(new Vocabulary(15, "Grandmother", "/ˈɡrænˌmʌðər/", 2));
        vocabularyList.add(new Vocabulary(16, "Grandfather", "/ˈɡrænˌfɑːðər/", 2));
        vocabularyList.add(new Vocabulary(17, "Aunt", "/ænt/", 2));
        vocabularyList.add(new Vocabulary(18, "Uncle", "/ˈʌŋkəl/", 2));
        vocabularyList.add(new Vocabulary(19, "Cousin", "/ˈkʌzən/", 2));
        vocabularyList.add(new Vocabulary(20, "Child", "/tʃaɪld/", 2));

        // Lesson 3: Food & Dining
        vocabularyList.add(new Vocabulary(21, "Restaurant", "/ˈrestərɑːnt/", 3));
        vocabularyList.add(new Vocabulary(22, "Menu", "/ˈmenjuː/", 3));
        vocabularyList.add(new Vocabulary(23, "Breakfast", "/ˈbrekfəst/", 3));
        vocabularyList.add(new Vocabulary(24, "Lunch", "/lʌntʃ/", 3));
        vocabularyList.add(new Vocabulary(25, "Dinner", "/ˈdɪnər/", 3));
        vocabularyList.add(new Vocabulary(26, "Delicious", "/dɪˈlɪʃəs/", 3));
        vocabularyList.add(new Vocabulary(27, "Water", "/ˈwɔːtər/", 3));
        vocabularyList.add(new Vocabulary(28, "Bill", "/bɪl/", 3));
        vocabularyList.add(new Vocabulary(29, "Vegetarian", "/ˌvedʒəˈteriən/", 3));
        vocabularyList.add(new Vocabulary(30, "Spicy", "/ˈspaɪsi/", 3));

        // Lesson 4: Travel Essentials
        vocabularyList.add(new Vocabulary(31, "Airport", "/ˈɛrpɔːrt/", 4));
        vocabularyList.add(new Vocabulary(32, "Passport", "/ˈpæspɔːrt/", 4));
        vocabularyList.add(new Vocabulary(33, "Hotel", "/hoʊˈtɛl/", 4));
        vocabularyList.add(new Vocabulary(34, "Ticket", "/ˈtɪkɪt/", 4));
        vocabularyList.add(new Vocabulary(35, "Luggage", "/ˈlʌɡɪdʒ/", 4));
        vocabularyList.add(new Vocabulary(36, "Reservation", "/ˌrezərˈveɪʃən/", 4));
        vocabularyList.add(new Vocabulary(37, "Departure", "/dɪˈpɑːrtʃər/", 4));
        vocabularyList.add(new Vocabulary(38, "Arrival", "/əˈraɪvəl/", 4));
        vocabularyList.add(new Vocabulary(39, "Tourist", "/ˈtʊrɪst/", 4));
        vocabularyList.add(new Vocabulary(40, "Direction", "/dəˈrɛkʃən/", 4));

        // Lesson 5: Shopping
        vocabularyList.add(new Vocabulary(41, "Store", "/stɔːr/", 5));
        vocabularyList.add(new Vocabulary(42, "Price", "/praɪs/", 5));
        vocabularyList.add(new Vocabulary(43, "Discount", "/ˈdɪskaʊnt/", 5));
        vocabularyList.add(new Vocabulary(44, "Size", "/saɪz/", 5));
        vocabularyList.add(new Vocabulary(45, "Cash", "/kæʃ/", 5));
        vocabularyList.add(new Vocabulary(46, "Credit card", "/ˈkrɛdɪt kɑːrd/", 5));
        vocabularyList.add(new Vocabulary(47, "Receipt", "/rɪˈsiːt/", 5));
        vocabularyList.add(new Vocabulary(48, "Sale", "/seɪl/", 5));
        vocabularyList.add(new Vocabulary(49, "Try on", "/traɪ ɒn/", 5));
        vocabularyList.add(new Vocabulary(50, "Expensive", "/ɪkˈspɛnsɪv/", 5));

        // Lesson 6: Weather & Seasons
        vocabularyList.add(new Vocabulary(51, "Sunny", "/ˈsʌni/", 6));
        vocabularyList.add(new Vocabulary(52, "Rainy", "/ˈreɪni/", 6));
        vocabularyList.add(new Vocabulary(53, "Cloudy", "/ˈklaʊdi/", 6));
        vocabularyList.add(new Vocabulary(54, "Snow", "/snoʊ/", 6));
        vocabularyList.add(new Vocabulary(55, "Temperature", "/ˈtɛmprətʃər/", 6));
        vocabularyList.add(new Vocabulary(56, "Forecast", "/ˈfɔːrkæst/", 6));
        vocabularyList.add(new Vocabulary(57, "Summer", "/ˈsʌmər/", 6));
        vocabularyList.add(new Vocabulary(58, "Winter", "/ˈwɪntər/", 6));
        vocabularyList.add(new Vocabulary(59, "Autumn", "/ˈɔːtəm/", 6));
        vocabularyList.add(new Vocabulary(60, "Spring", "/sprɪŋ/", 6));

        // Lesson 7: Business Communication
        vocabularyList.add(new Vocabulary(61, "Meeting", "/ˈmiːtɪŋ/", 7));
        vocabularyList.add(new Vocabulary(62, "Presentation", "/ˌprezənˈteɪʃən/", 7));
        vocabularyList.add(new Vocabulary(63, "Deadline", "/ˈdɛdˌlaɪn/", 7));
        vocabularyList.add(new Vocabulary(64, "Conference", "/ˈkɒnfərəns/", 7));
        vocabularyList.add(new Vocabulary(65, "Negotiate", "/nɪˈɡoʊʃieɪt/", 7));
        vocabularyList.add(new Vocabulary(66, "Contract", "/ˈkɒntrækt/", 7));
        vocabularyList.add(new Vocabulary(67, "Colleague", "/ˈkɒliːɡ/", 7));
        vocabularyList.add(new Vocabulary(68, "Schedule", "/ˈʃɛdjuːl/", 7));
        vocabularyList.add(new Vocabulary(69, "Budget", "/ˈbʌdʒɪt/", 7));
        vocabularyList.add(new Vocabulary(70, "Report", "/rɪˈpɔːrt/", 7));

        // Lesson 8: Academic Language
        vocabularyList.add(new Vocabulary(71, "Research", "/rɪˈsɜːrtʃ/", 8));
        vocabularyList.add(new Vocabulary(72, "Thesis", "/ˈθiːsɪs/", 8));
        vocabularyList.add(new Vocabulary(73, "Analysis", "/əˈnæləsɪs/", 8));
        vocabularyList.add(new Vocabulary(74, "Theory", "/ˈθɪəri/", 8));
        vocabularyList.add(new Vocabulary(75, "Bibliography", "/ˌbɪbliˈɒɡrəfi/", 8));
        vocabularyList.add(new Vocabulary(76, "Citation", "/saɪˈteɪʃən/", 8));
        vocabularyList.add(new Vocabulary(77, "Hypothesis", "/haɪˈpɒθəsɪs/", 8));
        vocabularyList.add(new Vocabulary(78, "Methodology", "/ˌmɛθəˈdɒlədʒi/", 8));
        vocabularyList.add(new Vocabulary(79, "Conclusion", "/kənˈkluːʒən/", 8));
        vocabularyList.add(new Vocabulary(80, "Abstract", "/ˈæbstrækt/", 8));

        // Lesson 9: Technology & Internet
        vocabularyList.add(new Vocabulary(81, "Website", "/ˈwɛbˌsaɪt/", 9));
        vocabularyList.add(new Vocabulary(82, "Download", "/ˌdaʊnˈloʊd/", 9));
        vocabularyList.add(new Vocabulary(83, "Password", "/ˈpæsˌwɜːrd/", 9));
        vocabularyList.add(new Vocabulary(84, "Software", "/ˈsɒftˌwɛr/", 9));
        vocabularyList.add(new Vocabulary(85, "Hardware", "/ˈhɑːrdˌwɛr/", 9));
        vocabularyList.add(new Vocabulary(86, "Wireless", "/ˈwaɪərləs/", 9));
        vocabularyList.add(new Vocabulary(87, "Database", "/ˈdeɪtəˌbeɪs/", 9));
        vocabularyList.add(new Vocabulary(88, "Algorithm", "/ˈælɡəˌrɪðəm/", 9));
        vocabularyList.add(new Vocabulary(89, "Encryption", "/ɪnˈkrɪpʃən/", 9));
        vocabularyList.add(new Vocabulary(90, "Cloud", "/klaʊd/", 9));
        
        db.vocabularyDao().insertAll(vocabularyList);
    }

    private static void insertDefaultQuizzes(AppDatabase db) {
        List<Quiz> quizzes = new ArrayList<>();
        
        // Lesson 1: Basic Greetings
        quizzes.add(new Quiz(1, "How ___ you?", "are", 1));
        quizzes.add(new Quiz(2, "What ___ your name?", "is", 1));
        quizzes.add(new Quiz(3, "Good ___, how are you?", "morning", 1));
        quizzes.add(new Quiz(4, "I'm ___ to meet you.", "pleased", 1));
        quizzes.add(new Quiz(5, "You should greet Dr. Smith ___ when meeting for the first time.", "formally", 1));
        quizzes.add(new Quiz(6, "When meeting someone for the first time, you should use a ___ greeting.", "formal", 1));
        quizzes.add(new Quiz(7, "It's appropriate to use ___ titles when greeting professionals.", "professional", 1));
        quizzes.add(new Quiz(8, "Good ___ is a formal greeting used in the evening.", "evening", 1));
        quizzes.add(new Quiz(9, "When greeting a female with unknown marital status, use ___.", "Ms", 1));
        quizzes.add(new Quiz(10, "___ you later is an informal way to say goodbye.", "See", 1));
        
        // Lesson 2: Family Members
        quizzes.add(new Quiz(11, "My mother's daughter is my ___.", "sister", 2));
        quizzes.add(new Quiz(12, "My mother's mother is my ___.", "grandmother", 2));
        quizzes.add(new Quiz(13, "My father's son is my ___.", "brother", 2));
        quizzes.add(new Quiz(14, "My aunt's children are my ___.", "cousins", 2));
        quizzes.add(new Quiz(15, "My sister's husband is my ___.", "brother-in-law", 2));
        quizzes.add(new Quiz(16, "My brother's daughter is my ___.", "niece", 2));
        quizzes.add(new Quiz(17, "My dad's brother is my ___.", "uncle", 2));
        quizzes.add(new Quiz(18, "My grandpa's father is my ___.", "great grandpa", 2));
        quizzes.add(new Quiz(19, "My female spouse is my ___.", "wife", 2));
        quizzes.add(new Quiz(20, "My step-mother's son is my ___.", "step-brother", 2));
        
        // Lesson 3: Food & Dining
        quizzes.add(new Quiz(21, "A place where you buy bread and cakes is called a ___.", "bakery", 3));
        quizzes.add(new Quiz(22, "The ___ shows all available food options in a restaurant.", "menu", 3));
        quizzes.add(new Quiz(23, "The first meal of the day is called ___.", "breakfast", 3));
        quizzes.add(new Quiz(24, "When food tastes very good, it is ___.", "delicious", 3));
        quizzes.add(new Quiz(25, "A person who doesn't eat meat is called a ___.", "vegetarian", 3));
        quizzes.add(new Quiz(26, "Food with a hot, pungent flavor is described as ___.", "spicy", 3));
        quizzes.add(new Quiz(27, "The ___ is what you ask for when you want to pay in a restaurant.", "bill", 3));
        quizzes.add(new Quiz(28, "The evening meal is commonly called ___.", "dinner", 3));
        quizzes.add(new Quiz(29, "The midday meal is called ___.", "lunch", 3));
        quizzes.add(new Quiz(30, "A ___ is a place where you buy food and household items.", "grocery store", 3));
        
        // Lesson 4: Travel Essentials
        quizzes.add(new Quiz(31, "I need to ___ a flight for my vacation.", "book", 4));
        quizzes.add(new Quiz(32, "The plane ___ at 3 PM.", "departs", 4));
        quizzes.add(new Quiz(33, "Can I have a window ___?", "seat", 4));
        quizzes.add(new Quiz(34, "Where is the baggage ___?", "claim", 4));
        quizzes.add(new Quiz(35, "You need a ___ to travel internationally.", "passport", 4));
        quizzes.add(new Quiz(36, "A ___ is a place where planes land and take off.", "airport", 4));
        quizzes.add(new Quiz(37, "You need to make a ___ to secure your hotel room.", "reservation", 4));
        quizzes.add(new Quiz(38, "Your ___ contains your clothes and personal items for travel.", "luggage", 4));
        quizzes.add(new Quiz(39, "The ___ is the tallest mountain in the world.", "Mount Everest", 4));
        quizzes.add(new Quiz(40, "The Nile is the ___ river in the world.", "longest", 4));
        
        // Lesson 5: Shopping
        quizzes.add(new Quiz(41, "A ___ is a shop where you buy medicine.", "pharmacy", 5));
        quizzes.add(new Quiz(42, "A ___ store is where you buy computers and TVs.", "electronics", 5));
        quizzes.add(new Quiz(43, "A place to buy rings and necklaces is a ___ store.", "jewelry", 5));
        quizzes.add(new Quiz(44, "The ___ of this item is too high.", "price", 5));
        quizzes.add(new Quiz(45, "Can I get a ___ on this item?", "discount", 5));
        quizzes.add(new Quiz(46, "I need to ___ these clothes before buying them.", "try on", 5));
        quizzes.add(new Quiz(47, "This item is on ___, so it costs less than usual.", "sale", 5));
        quizzes.add(new Quiz(48, "I'll pay with my ___ card instead of cash.", "credit", 5));
        quizzes.add(new Quiz(49, "Please give me a ___ for my purchase.", "receipt", 5));
        quizzes.add(new Quiz(50, "This product is very ___; it costs a lot of money.", "expensive", 5));
        
        // Lesson 6: Weather & Seasons
        quizzes.add(new Quiz(51, "Today is a ___ day with no clouds in the sky.", "sunny", 6));
        quizzes.add(new Quiz(52, "Don't forget your umbrella, it's a ___ day.", "rainy", 6));
        quizzes.add(new Quiz(53, "The sky is ___ today, we can't see the sun.", "cloudy", 6));
        quizzes.add(new Quiz(54, "In winter, we often see ___ falling from the sky.", "snow", 6));
        quizzes.add(new Quiz(55, "The ___ today is 25 degrees Celsius.", "temperature", 6));
        quizzes.add(new Quiz(56, "The weather ___ says it will rain tomorrow.", "forecast", 6));
        quizzes.add(new Quiz(57, "The hottest season of the year is ___.", "summer", 6));
        quizzes.add(new Quiz(58, "The coldest season of the year is ___.", "winter", 6));
        quizzes.add(new Quiz(59, "Another name for fall is ___.", "autumn", 6));
        quizzes.add(new Quiz(60, "The season that comes after winter is ___.", "spring", 6));
        
        // Lesson 7: Business Communication
        quizzes.add(new Quiz(61, "Let's ___ the meeting at 9 AM.", "schedule", 7));
        quizzes.add(new Quiz(62, "Could you ___ the presentation with the team?", "share", 7));
        quizzes.add(new Quiz(63, "We need to ___ this issue in our meeting.", "discuss", 7));
        quizzes.add(new Quiz(64, "The ___ will be sent after the meeting.", "minutes", 7));
        quizzes.add(new Quiz(65, "We need to meet the project ___ by Friday.", "deadline", 7));
        quizzes.add(new Quiz(66, "I'll prepare a ___ for the board meeting.", "presentation", 7));
        quizzes.add(new Quiz(67, "We should ___ with the client about the terms.", "negotiate", 7));
        quizzes.add(new Quiz(68, "Please sign the ___ before we proceed.", "contract", 7));
        quizzes.add(new Quiz(69, "My ___ will attend the meeting in my place.", "colleague", 7));
        quizzes.add(new Quiz(70, "We need to stay within the ___ for this project.", "budget", 7));
        
        // Lesson 8: Academic Language
        quizzes.add(new Quiz(71, "The ___ shows interesting results.", "data", 8));
        quizzes.add(new Quiz(72, "We need to ___ more sources in our paper.", "cite", 8));
        quizzes.add(new Quiz(73, "This research ___ was published last year.", "paper", 8));
        quizzes.add(new Quiz(74, "The ___ supported our hypothesis.", "findings", 8));
        quizzes.add(new Quiz(75, "We should ___ the methodology carefully.", "review", 8));
        quizzes.add(new Quiz(76, "A ___ is the main argument of your research paper.", "thesis", 8));
        quizzes.add(new Quiz(77, "The ___ section summarizes your research paper.", "abstract", 8));
        quizzes.add(new Quiz(78, "We conducted an ___ of the experimental results.", "analysis", 8));
        quizzes.add(new Quiz(79, "The ___ lists all sources used in your paper.", "bibliography", 8));
        quizzes.add(new Quiz(80, "A ___ is an educated guess that can be tested.", "hypothesis", 8));
        
        // Lesson 9: Technology & Internet
        quizzes.add(new Quiz(81, "I need to ___ this file to my computer.", "download", 9));
        quizzes.add(new Quiz(82, "Create a strong ___ for your online accounts.", "password", 9));
        quizzes.add(new Quiz(83, "The company's ___ provides information about their services.", "website", 9));
        quizzes.add(new Quiz(84, "Computer programs are also called ___.", "software", 9));
        quizzes.add(new Quiz(85, "Physical computer components are called ___.", "hardware", 9));
        quizzes.add(new Quiz(86, "A ___ connection doesn't need cables.", "wireless", 9));
        quizzes.add(new Quiz(87, "A collection of organized information is called a ___.", "database", 9));
        quizzes.add(new Quiz(88, "An ___ is a step-by-step procedure for calculations.", "algorithm", 9));
        quizzes.add(new Quiz(89, "___ protects your data from unauthorized access.", "encryption", 9));
        quizzes.add(new Quiz(90, "Data stored on the ___ can be accessed from anywhere.", "cloud", 9));
        
        // Insert all quizzes into the database
        db.quizDao().insertAll(quizzes);
    }
} 