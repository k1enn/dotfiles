import 'package:flutter/material.dart';
import '../models/food_category.dart';
import 'category_card.dart';
// 1
class CategorySection extends StatelessWidget {
  final List<FoodCategory> categories;
  const CategorySection({super.key, required this.categories});
  @override
  Widget build(BuildContext context) {
    // 2
    return Padding(
      padding: const EdgeInsets.all(8.0),
      // 3
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // 4
          const Padding(
            padding: EdgeInsets.only(left: 16.0, bottom: 8.0),
            child: Text(
              'Categories',
              style: TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
          // 5
          SizedBox(
            height: 275,
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              itemCount: categories.length,
              itemBuilder: (context, index) {
                return SizedBox(
                  width: 200,
                  child: CategoryCard(category: categories[index]),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}