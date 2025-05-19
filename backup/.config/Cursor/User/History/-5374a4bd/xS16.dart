import 'package:flutter/material.dart';
import '../models/restaurant.dart';

class RestaurantLandscapeCard extends StatelessWidget {
  final Restaurant restaurant;

  const RestaurantLandscapeCard({
    super.key,
    required this.restaurant,
  });

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context)
      .textTheme
      .apply(
        displayColor: Theme.of(context)
          .colorScheme
          .onSurface
      );
    return Card(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.min,
        children: [
          // Add Image
          // Add ListTile
        ],
      ),
    );
  }
}