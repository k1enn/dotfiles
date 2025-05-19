import 'package:flutter/material.dart';
import '../models/post.dart';

class PostCard extends StatelessWidget {
  final Post post;
  const PostCard({
    super.key,
    required this.post,
  });
  @override
  Widget build(BuildContext context) {
    Theme.of(context).textTheme.apply(
          displayColor: Theme.of(context).colorScheme.onSurface,
        );
    return const Card(
      child: Padding(
        padding: EdgeInsets.all(16.0),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
// TODO: Add CircleAvatar
// TODO: Add spacing
// TODO: Add Expanded Widget
          ],
        ),
      ),
    );
  }
}
