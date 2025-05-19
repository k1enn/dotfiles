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
        mainAxisSize: MainAxisSize.min,
        children: [
          ClipRRect(
            borderRadius: 
            const BorderRadius.vertical(top: Radius.circular(8.0),),
          )
          // Add ListTile
        ],
      ),
    );
  }
}