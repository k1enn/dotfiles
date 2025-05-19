import 'package:flutter/material.dart';
import '../models/food_category.dart';

class CategoryCard extends StatelessWidget {
// 1
  final FoodCategory category;
  const CategoryCard({
    super.key,
    required this.category,
  });
  @override
  Widget build(BuildContext context) {
// TODO: Get text theme
  final textTheme = Theme.of(context)
      .textTheme
      .apply(displayColor: Theme.of(context).colorScheme.onSurface);
// TODO: Replace with Card widget
    return Card(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Add Stack widget
          // Add ListTile
        ],
      )
    );
  }
}
