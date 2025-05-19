public List<Lesson> getAllLessons() {
    try {
        Future<List<Lesson>> future = AppDatabase.databaseWriteExecutor.submit(() -> 
            lessonDao.getAllLessons());
        return future.get();
    } catch (ExecutionException | InterruptedException e) {
        Log.e(TAG, "Error getting all lessons", e);
        return new ArrayList<>();
    }
} 