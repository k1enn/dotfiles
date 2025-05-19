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
            return ListView(
              shrinkWrap: true,
              scrollDirection: Axis.vertical,
              children: [
                RestaurantSection(restaurants: restaurants),
                // TODO: Add CategorySection
                Container(
                  height: 300,
                  color: Colors.green,
                ),
                // TODO: Add PostSection
                Container(
                  height: 300,
                  color: Colors.orange,
                ),
              ],
            );
            } else {
            return const Center(
              child: CircularProgressIndicator(),
            );
          }
        });
// 2
  }
}
