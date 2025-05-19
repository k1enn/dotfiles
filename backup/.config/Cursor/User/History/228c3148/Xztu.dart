// 1
import 'package:flutter/material.dart';
import '../models/restaurant.dart';

// 2
class RestaurantPage extends StatefulWidget {
  final Restaurant restaurant;
// 3
  const RestaurantPage({
    super.key,
    required this.restaurant,
  });
}

@override
State<RestaurantPage> createState() => _RestaurantPageState();

// 4
class _RestaurantPageState extends State<RestaurantPage> {
// TODO: Add Desktop Threshold
// TODO: Add Constraint Properties
// TODO: Calculate Constrained Width
// TODO: Add Calculate Column Count
// TODO: Build Custom Scroll View
// TODO: Build Sliver App Bar
// TODO: Build Info Section
// TODO: Build Grid Item
// TODO: Build Section Title
// TODO: Build Grid View
// TODO: Build Grid View Section
// TODO: Replace build method
  @override
  Widget build(BuildContext context) {
// 5
    return Scaffold(
      body: Center(
// TODO: Replace with Custom Scroll View
        child: Text(
          'Restaurant Page',
          style: TextStyle(fontSize: 16.0),
        ),
      ),
    );
  }
}
