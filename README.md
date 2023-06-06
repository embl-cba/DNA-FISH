# Analyse FISH Spots - Fiji Plugin

A Fiji plugin to analyse images with FISH spots.

## Installation

- Please [install Fiji](fiji.sc)
    - Note that in case of doubt starting from a fresh Fiji is recommended 
- Download the desired (latest) version of [the jar](https://github.com/tischi/fiji-plugin-FISH/tree/master/jars) into the `Fiji.app/jars` folder
- Restart Fiji
- The plugin will appear under **[ Plugins > Analyze > FISH Spots ]** <img src="https://user-images.githubusercontent.com/2157566/79047667-6b54cb80-7c18-11ea-84ff-7d1d29fe6570.png" width="500">

Notes: 

1. a previous version of this plugin was distributed as a jar with a name similar to open-stacks-as-virtual-stacks.jar. This jar may be in your Fiji plugins or jars folder and **must be removed**.
2. another previous version of this plugin was distributed via the EMBL-CBA update site; this is deprecated and this update site **must not be used anymore**.

## Usage

TODO...

### Channel setup

TODO...

### Background measurement

TODO...

### Spot position measurement

Spots are detected using TrackMate; help can be [found here](https://imagej.net/TrackMate_Algorithms#Spot_features_generated_by_the_spot_detectors).

### Manual region selection

Using ImageJ's point selection tool one marks regions in which FISH spots should be analyzed (see below). It is *not important* to place these points very exactly, because the algorithm will just use these annotations to identify the regions in which it should detect the FISH spots (see below). 

### Analyze spots

Upon pressing the **[ Analyze spots ]** button (fka "Analyze regions"), the plugin will, for each of the manually selected points, find the *closest* spot in each channel, measure its position and measure the distances between the spots in the different channels. 

### TrackMate_DoG

Given d an approximate expected particle diameter, determined upon inspection, two gaussian filters are produced with standard deviation σ₁ and σ₂:

σ₁ = 1 / (1 + √2 ) × d

σ₂ = √2 × σ₁

The image is filtered using these two gaussians, and the result of the second filter (largest sigma) is subtracted from the result of the first filter (smallest sigma). This yields a smoothed image with sharp local maximas at particle locations. A detection spot is then created for each of these maximas, and an arbitrary quality feature is assigned to the new spot by taking the smoothed image value at the maximum. If two spots are found to be closer than the expected radius d/2, the one with the lowest quality is discarded.

#### TrackMate_DoG_SubPixel

To improve the localization accuracy, and extra step is taken to yield a sub-pixel localization of the spots. The position of each spot is recalculated using a simple parabolic interpolation scheme, as in {David G. Lowe, "Distinctive image features from scale-invariant keypoints", International Journal of Computer Vision, 60, 2 (2004), pp. 91-110.}.


