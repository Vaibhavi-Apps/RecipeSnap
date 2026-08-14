package com.official.recipesnap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MealPlanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MealPlanRepository(application)
    
    private val _isSetupCompleted = MutableStateFlow(repository.isSetupCompleted())
    val isSetupCompleted: StateFlow<Boolean> = _isSetupCompleted.asStateFlow()

    private val _myMeals = MutableStateFlow<List<MyMeal>>(emptyList())
    val myMeals: StateFlow<List<MyMeal>> = _myMeals.asStateFlow()

    private val _plannedMeals = MutableStateFlow<List<PlannedMeal>>(emptyList())
    val plannedMeals: StateFlow<List<PlannedMeal>> = _plannedMeals.asStateFlow()

    private val _currentWeekStart = MutableStateFlow(getStartOfWeek(Date()))
    val currentWeekStart: StateFlow<Date> = _currentWeekStart.asStateFlow()
    
    private val _selectedDate = MutableStateFlow(clearTime(Date()))
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _myMeals.value = repository.getMyMeals()
        _plannedMeals.value = repository.getPlannedMeals()
    }

    fun completeSetup(selectedMeals: List<MyMeal>) {
        repository.saveMyMeals(selectedMeals)
        repository.setSetupCompleted(true)
        _isSetupCompleted.value = true
        loadData()
    }

    fun addCustomMeal(meal: MyMeal) {
        repository.saveMyMeal(meal)
        loadData()
    }
    
    fun removeMyMeal(mealId: String) {
        repository.deleteMyMeal(mealId)
        loadData()
    }
    
    fun addPlannedMeal(date: Date, category: MealCategory, myMeal: MyMeal) {
        // If myMeal is not in MyMeals (e.g. from Recipe Ideas), save it first
        if (_myMeals.value.none { it.id == myMeal.id }) {
            repository.saveMyMeal(myMeal)
        }
        val dateString = formatDateForStorage(date)
        val plannedMeal = PlannedMeal(date = dateString, mealCategory = category, myMealId = myMeal.id)
        repository.savePlannedMeal(plannedMeal)
        loadData()
    }
    
    fun removePlannedMeal(plannedMealId: String) {
        repository.deletePlannedMeal(plannedMealId)
        loadData()
    }

    fun selectDate(date: Date) {
        _selectedDate.value = clearTime(date)
        val startOfWeek = getStartOfWeek(date)
        if (startOfWeek != _currentWeekStart.value) {
            _currentWeekStart.value = startOfWeek
        }
    }
    
    fun nextWeek() {
        val cal = Calendar.getInstance().apply { time = _currentWeekStart.value }
        cal.add(Calendar.DAY_OF_YEAR, 7)
        _currentWeekStart.value = cal.time
        _selectedDate.value = cal.time
    }
    
    fun previousWeek() {
        val cal = Calendar.getInstance().apply { time = _currentWeekStart.value }
        cal.add(Calendar.DAY_OF_YEAR, -7)
        _currentWeekStart.value = cal.time
        _selectedDate.value = cal.time
    }
    
    fun getWeekDays(): List<Date> {
        val days = mutableListOf<Date>()
        val cal = Calendar.getInstance().apply { time = _currentWeekStart.value }
        for (i in 0 until 7) {
            days.add(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return days
    }
    
    fun getPlannedMealsForDate(date: Date): List<PlannedMeal> {
        val dateStr = formatDateForStorage(date)
        return _plannedMeals.value.filter { it.date == dateStr }
    }
    
    fun getMyMealById(id: String): MyMeal? {
        return _myMeals.value.find { it.id == id }
    }

    private fun getStartOfWeek(date: Date): Date {
        val cal = Calendar.getInstance().apply { 
            time = date
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            clear(Calendar.MINUTE)
            clear(Calendar.SECOND)
            clear(Calendar.MILLISECOND)
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }
        return cal.time
    }
    
    private fun clearTime(date: Date): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            clear(Calendar.MINUTE)
            clear(Calendar.SECOND)
            clear(Calendar.MILLISECOND)
        }.time
    }

    fun formatDateForStorage(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
    }
    
    fun formatDateRange(start: Date): String {
        val endCal = Calendar.getInstance().apply { time = start; add(Calendar.DAY_OF_YEAR, 6) }
        val startFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        
        val startCal = Calendar.getInstance().apply { time = start }
        val endFormat = if (startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH)) {
            SimpleDateFormat("d, yyyy", Locale.getDefault())
        } else {
            SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        }
        return "${startFormat.format(start)}–${endFormat.format(endCal.time)}"
    }
}
