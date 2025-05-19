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
        _buildSliverAppBar(),
        _buildInfoSection(),
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

  SliverAppBar _buildSliverAppBar() {
    return SliverAppBar(
      pinned: true,
      expandedHeight: 300.0,
      flexibleSpace: FlexibleSpaceBar(
        background: Center(
            child: Padding(
                padding: const EdgeInsets.only(
                  left: 16.0,
                  right: 16.0,
                  top: 64.0,
                ),
                child: Stack(
                  children: [
                    Container(
                      margin: const EdgeInsets.only(
                        bottom: 30.0,
                      ),
                      decoration: BoxDecoration(
                        color: Colors.grey,
                        borderRadius: BorderRadius.circular(16.0),
                        image: DecorationImage(
                          image: AssetImage(widget.restaurant.imageUrl),
                          fit: BoxFit.cover,
                        ),
                      ),
                    ),
                    const Positioned(
                      bottom: 0.0,
                      left: 16.0,
                      child: CircleAvatar(
                        radius: 30,
                        child: Icon(Icons.store, color: Colors.white),
                      )
                    )
                  ],
                ))),
      ),
    );
  }

  SliverToBoxAdapter _buildInfoSection() {
    final textTheme = Theme.of(context).textTheme;
    final restaurant = widget.restaurant;
    return SliverToBoxAdapter(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(restaurant.name, style: textTheme.headlineLarge,),
            Text(restaurant.address, style: textTheme.bodySmall),
            Text(restaurant.getRatingAndDistance(), style: textTheme.bodySmall),
          ],
        ),
      ),
    );
    
  }
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
