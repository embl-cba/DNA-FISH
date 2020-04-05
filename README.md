# Analyse FISH Spots - Fiji Plugin

A Fiji plugin to analyse images with FISH spots.

## Installation

## Usage

## Spot position measurement

From TrackMate help: https://imagej.net/TrackMate_Algorithms#Spot_features_generated_by_the_spot_detectors

### TrackMate_DoG

Given d an approximate expected particle diameter, determined upon inspection, two gaussian filters are produced with standard deviation σ₁ and σ₂:

σ₁ = 1 / (1 + √2 ) × d

σ₂ = √2 × σ₁

The image is filtered using these two gaussians, and the result of the second filter (largest sigma) is subtracted from the result of the first filter (smallest sigma). This yields a smoothed image with sharp local maximas at particle locations. A detection spot is then created for each of these maximas, and an arbitrary quality feature is assigned to the new spot by taking the smoothed image value at the maximum. If two spots are found to be closer than the expected radius d/2, the one with the lowest quality is discarded.

#### TrackMate_DoG_SubPixel

To improve the localization accuracy, and extra step is taken to yield a sub-pixel localization of the spots. The position of each spot is recalculated using a simple parabolic interpolation scheme, as in {David G. Lowe, "Distinctive image features from scale-invariant keypoints", International Journal of Computer Vision, 60, 2 (2004), pp. 91-110.}.


