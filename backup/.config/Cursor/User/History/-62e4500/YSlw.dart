import 'package:flutter/material.dart';
import '../api/mock_yummy_service.dart';
import '../components/restaurant_section.dart';

class ExplorePage extends StatelessWidget {
// 1
  final mockService = MockYummyService();
  ExplorePage({super.key});

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
        future: mockService.getExploreData(),
        builder: (context, AsyncSnapshot<ExploreData> snapshots) {
          if (snapshots.connectionState == ConnectionState.done) {
            final restaurants = snapshots.data?.restaurants ?? [];
            final categories = snapshots.data?.categories ?? [];
            final posts = snapshots.data?.friendPosts ?? [];
            return RestaurantSection(restaurants: restaurants);
            } else {
            return const Center(
              child: CircularProgressIndicator(),
            );
          }
        });
// 2
  }
}
