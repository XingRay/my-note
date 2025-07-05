# FaceLandmarkGraph

mediapipe/tasks/cc/vision/face_landmarker/face_landmarker_graph.cc



params:

image_in



1 检测输入图像中的人脸

"mediapipe.tasks.vision.face_detector.FaceDetectorGraph"

options: FaceDetectorGraphOptions

in["IMAGE"] <--> image_in

输入: 图像 image

输出: 
EXPANDED_FACE_RECTS : 



2 将输出中的数据的数量限制到 max_num_faces 个

"ClipNormalizedRectVectorSizeCalculator"

option: ClipVectorSizeCalculatorOptions

​	set_max_vec_size(max_num_faces);

output: clipped_face_rects



3 检测人脸关键点

"mediapipe.tasks.vision.face_landmarker.MultiFaceLandmarksDetectorGraph"
face_landmarks_detector_graph

  option: FaceLandmarksDetectorGraphOptions

face_landmarks_detector_graph.in[ "IMAGE" ] < -- >image_in

face_landmarks_detector_graph.in["NORM_RECT"] < -- > clipped_face_rects

face_landmarks_detector_graph.out["NORM_LANDMARKS"] < -- > face_landmarks : list

face_landmarks_detector_graph.out["FACE_RECTS_NEXT_FRAME"] < -- > face_rects_for_next_frame



"mediapipe.tasks.vision.face_landmarker.SingleFaceLandmarksDetectorGraph"

in["IMAGE"] <-->  image

in["NORM_RECT"] <--> face_rect

out["PRESENCE"] <--> presence

out["PresenceScore"] <--> presence_score

out["FaceRectNextFrame"] <--> face_rect_next_frame

out["NormLandmarks"] <-->landmarks



"EndLoopBooleanCalculator"



"LandmarksSmoothingCalculator"

object_scale  = (width + height) / 2

out_landmarks = landmarks_filter_( in_landmarks, timestamp, object_scale )

landmarks_filter_:



空过滤器: 输入 => 输出

```
NoFilter
  out_landmarks = in_landmarks
```



矢量滤波器 in.x/y/z -- filter -> out.x/y/z

```
VelocityFilter
    int window_size_;
    float velocity_scale_;
    float min_allowed_object_scale_;
    bool disable_value_scaling_;

    std::vector< RelativeVelocityFilter > x_filters_;
    std::vector< RelativeVelocityFilter > y_filters_;
    std::vector< RelativeVelocityFilter > z_filters_;
```



RelativeVelocityFilter

```
RelativeVelocityFilter
	if distance_mode_ == DistanceEstimationMode::kLegacyTransition:
		distance = value * value_scale - last_value_ * last_value_scale_
	else
		distance = value_scale * (value - last_value_)
		
	const int64_t duration = new_timestamp - last_timestamp_;

    float cumulative_distance = distance;
    int64_t cumulative_duration = duration;

    // Define max cumulative duration assuming
    // 30 frames per second is a good frame rate, so assuming 30 values
    // per second or 1 / 30 of a second is a good duration per window element
    constexpr int64_t kAssumedMaxDuration = 1000000000 / 30;
    const int64_t max_cumulative_duration =
        (1 + window_.size()) * kAssumedMaxDuration;
    for (const auto& el : window_) {
      if (cumulative_duration + el.duration > max_cumulative_duration) {
        // This helps in cases when durations are large and outdated
        // window elements have bad impact on filtering results
        break;
      }
      cumulative_distance += el.distance;
      cumulative_duration += el.duration;
    }

    constexpr double kNanoSecondsToSecond = 1e-9;
    const float velocity =
        cumulative_distance / (cumulative_duration * kNanoSecondsToSecond);
    alpha = 1.0f - 1.0f / (1.0f + velocity_scale_ * std::abs(velocity));
    window_.push_front({distance, duration});
    if (window_.size() > max_window_size_) {
      window_.pop_back();
    }
  }

  last_value_ = value;
  last_value_scale_ = value_scale;
  last_timestamp_ = new_timestamp;

  return low_pass_filter_.ApplyWithAlpha(value, alpha);
```



LowPassFilter 低通滤波器

返回值为旧值与新值的线性插值

```
LowPassFilter(value, alpha)
	result = alpha_ * value + (1.0 - alpha_) * stored_value_;
	raw_value_ = value;
	stored_value_ = result;
```



OneEuroFilter 一欧滤波器
**低速抑抖，高速减滞** 的自适应策略

```
OneEuroFilter
  double frequency_;
  double min_cutoff_;
  double beta_;
  double derivate_cutoff_;
  std::unique_ptr<LowPassFilter> x_;
  std::unique_ptr<LowPassFilter> dx_;
  int64_t last_time_;
  
apply:
  int64_t new_timestamp = absl::ToInt64Nanoseconds(timestamp);
  if (last_time_ >= new_timestamp) {
    // Results are unpredictable in this case, so nothing to do but
    // return same value
    ABSL_LOG(WARNING) << "New timestamp is equal or less than the last one.";
    return value;
  }

  // update the sampling frequency based on timestamps
  if (last_time_ != 0 && new_timestamp != 0) {
    static constexpr double kNanoSecondsToSecond = 1e-9;
    frequency_ = 1.0 / ((new_timestamp - last_time_) * kNanoSecondsToSecond);
  }
  last_time_ = new_timestamp;

  // estimate the current variation per second
  double dvalue = x_->HasLastRawValue()
                      ? (value - x_->LastRawValue()) * value_scale * frequency_
                      : 0.0;  // FIXME: 0.0 or value?
  double edvalue = dx_->ApplyWithAlpha(dvalue, GetAlpha(derivate_cutoff_));
  // use it to update the cutoff frequency
  double cutoff = min_cutoff_ + beta_ * std::fabs(edvalue);

  // filter the given value
  return x_->ApplyWithAlpha(value, GetAlpha(cutoff));
```

​		



"ImagePropertiesCalculator"

input

​	IMAGE 
​	NORM_RECT 

output 

​	TENSORS 

​	MATRIX  : float[16],  4x4 row-major-order matrix   input * M => output,  output * M.inverse() => input

​	LETTERBOX_PADDING: float[4] => output.padding [left, top, right, bottom] norm [0.0f ~ 1.0f], 需要 keep_aspect_ratio

​	IMAGE_SIZE int[2]  { input.width input.height }

​	IMAGE : inputImage  todo : Replace PassThroughCalculator



if (face_detector_options.num_faces > 1)
	face_landmarks_detector_graph.set_smooth_landmarks( use_stream_mode() )
else

​	if(face_landmarks_detector_graph.smooth_landmarks)  => error: face landmarks smoothing only support a single face





"ImageToTensorCalculator"

```
input_stream: "IMAGE:image"  # or "IMAGE_GPU:image"
input_stream: "NORM_RECT:roi"

output_stream: "TENSORS:tensors"
output_stream: "MATRIX:matrix"

output_tensor_width: 256
output_tensor_height: 256
keep_aspect_ratio: false
output_tensor_float_range {
	min: 0.0
	max: 1.0
}
```





"ImageCloneCalculator"







"PreviousLoopbackCalculator"

in["MAIN"] <--> image_in

out["PREV_LOOP"] -> prev_face_rects_from_landmarks


"NormalizedRectVectorHasMinSizeCalculator"
in["ITERABLE"] < -- > prev_face_rects_from_landmarks

options: CollectionHasMinSizeCalculatorOptions
		set_min_size( max_num_faces )

out[""] -> has_enough_faces







if(has_enough_faces)

​		graph.remove( image_in )

image_for_face_detector : if( has_enough_faces )  face_detector.in["image"].disconnect()



if( params.norm_rect_in.has_value() )

   	norm_rect_in_for_face_detector : if( has_enough_faces)  graph.in["norm_rect_in"].disconnect()

norm_rect_in_for_face_detector < -- > face_detector.In("NORM_RECT")

expanded_face_rects_from_face_detector < -- > face_detector.out["EXPANDED_FACE_RECTS"]







"AssociationNormRectCalculator" face_association
options: AssociationCalculatorOptions
	set_min_similarity_threshold: params.min_tracking_confidence()


prev_face_rects_from_landmarks < -- > face_association.in[ "" ] [ 0 ]

expanded_face_rects_from_face_detector < -- > face_association.in[ "" ] [ 1 ]

face_association.out[""] < -- > face_rects < -- > clip_face_rects.in[""]







"FlowLimiterCalculator"

 input_stream: "IMAGE:image_in"

  input_stream: "NORM_RECT:norm_rect_in"

  output_stream: "IMAGE:image_in"

  output_stream: "NORM_RECT:norm_rect_in"
