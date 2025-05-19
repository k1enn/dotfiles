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

  @override
  State<RestaurantPage> createState() => _RestaurantPageState();
}

// 4
class _RestaurantPageState extends State<RestaurantPage> {
// TODO: Add Desktop Threshold
// TODO: Add Constraint Properties
// TODO: Calculate Constrained Width
// TODO: Add Calculate Column Count
  CustomScrollView _buildCustomScrollView() {
    return CustomScrollView(
      slivers: [
        //TODO: Add Sliver App bar
        SliverToBoxAdapter(
          child: Container(
            height: 200.0,
            color: Colors.red,
          ),
        ),
        // TODO: Add Restaurant Info Section
        SliverToBoxAdapter(
          child: Container(
            height: 300.0,
            color: Colors.green,
          ),
        ),
        // TODO: Add Menu Item Grid View Section
        SliverFillRemaining(
            child: Container(
          color: Colors.blue,
        ))
      ],
    );
  }

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
        child: _buildCustomScrollView(),
      ),
    );
  }
}
