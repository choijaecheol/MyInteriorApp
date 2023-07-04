package com.example.myinteriorapp.activities;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.contract.ActivityResultContracts;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
//import android.support.v4.content.FileProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;

import com.example.myinteriorapp.R;
import com.example.myinteriorapp.fragments.ARFragment;
import com.example.myinteriorapp.models.Furniture;
import com.example.myinteriorapp.models.ImageData;
import com.example.myinteriorapp.models.Place;
import com.example.myinteriorapp.network.ApiClient;
import com.example.myinteriorapp.network.ApiInterface;
import com.example.myinteriorapp.network.ApiResponse;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    ApiInterface api;
    private String currentPhotoPath; // currentPhotoPath 변수 선언

    private ProgressDialog progressDialog; // 전역 변수로 선언

    private boolean isModelLoaded = false;
    private ARFragment arFragment;
    private final ActivityResultLauncher<Uri> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), result -> {
                if (result) {
                    // 사진 촬영이 성공적으로 완료되었을 때의 처리 로직
                    // 촬영한 사진 데이터를 ImageData 형식으로 변환하여 사용
                    ImageData imageData = processCapturedPhoto();
                    // 이미지 데이터를 사용하여 API 호출 등의 처리 수행
                    performAIRecommendation(imageData);
                } else {
                    // 사진 촬영이 취소되었거나 실패한 경우의 처리 로직
                    Toast.makeText(this, "사진 촬영이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void capturePhoto() {
        // 카메라 앱을 실행하여 사진을 촬영하는 인텐트 생성
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // 사진을 저장할 임시 파일 생성
            File photoFile = null;
            try {
                photoFile = createImageFile(); //createImageFile 메소드 호출
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // 임시 파일이 정상적으로 생성되었을 경우에만 인텐트 실행
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this, "com.example.myinteriorapp.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraActivityResultLauncher.launch(photoUri);
            }
        }
    }


    private File createImageFile() throws IOException {
        // 이미지 파일을 임시로 저장할 디렉토리 생성
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdirs(); //mkdirs() 메서드는 디렉토리를 생성하고 그 결과를 나타내는 부울 값을 반환합니다.
        }

        // 임시 파일 이름 생성 (현재 시간 기반)
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";

        // 임시 파일 생성
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

        currentPhotoPath = imageFile.getAbsolutePath(); // currentPhotoPath 설정

        return imageFile;
    }


    /*
     capturePhoto() 메소드를 통해 카메라 앱을 실행하여 사진을 촬영하고,
     createImageFile() 메소드를 사용하여 임시 파일을 생성합니다.
     그 후 onActivityResult() 메소드에서 사진 촬영이 완료되었을 때의 처리를 수행합니다.
     */
    private ImageData processCapturedPhoto() {
        // 촬영한 사진 데이터를 ImageData 형식으로 변환하는 로직을 구현
        // Access the temporary photo file
        File photoFile = new File(currentPhotoPath);

        // Get the file's absolute path and create an ImageData object
        String imageId = UUID.randomUUID().toString(); // Generate a unique ID for the image
        String imagePath = photoFile.getAbsolutePath();

        return new ImageData(imageId, imagePath);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // Replace the FrameLayout container with ARFragment
            arFragment = new ARFragment(); // ARFragment 인스턴스 생성
            getSupportFragmentManager().beginTransaction() // ARFragment를 액티비티에 추가
                    .replace(R.id.ar_fragment_container, arFragment)
                    .commit();
        }

        // api
        api = ApiClient.getClient().create(ApiInterface.class);

        Button recommendationButton = findViewById(R.id.recommendationButton);
        recommendationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission(); //카메라 권한 체크
                //capturePhoto(); // 변경: capturePhoto() 메소드 호출로 변경
            }
        });

        //ar 버튼 클릭시
        Button arButton = findViewById(R.id.arButton);
        arButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AR 시작 버튼이 클릭되었을 때 동작을 정의합니다.
                startAR();
            }
        });

        arFragment.setOnTapArPlaneListener((HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
            if (!isModelLoaded) {
                addModelToScene(hitResult.createAnchor());
            }
        });

    }

    private void addModelToScene(Anchor anchor) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);

        // 모델을 로드하고 Renderable 객체 생성
        ModelRenderable.builder()
                .setSource(this, R.raw.rounded_chair)  // 모델 리소스를 지정
                .build()
                .thenAccept(modelRenderable -> {
                    // 모델 로드가 성공한 경우에만 실행될 콜백
                    transformableNode.setRenderable(modelRenderable);

                    arFragment.getArSceneView().getScene().addChild(anchorNode);
                    transformableNode.select();
                })
                .exceptionally(throwable -> {
                    // 모델 로드 실패 시 예외 처리
                    Toast.makeText(this, "Failed to load model renderable.", Toast.LENGTH_SHORT).show();
                    Log.e("TAG", "Failed to load model renderable.", throwable);
                    return null;
                });
    }


    private void startAR() {
        // AR 시작 동작을 처리합니다.

        // ARCore와 관련된 코드를 여기에 추가할 수 있습니다.
        if (arFragment != null) {
            try {
                arFragment.getArSceneView().resume();
                // 모델을 로드하고 표시하는 작업
                Toast.makeText(this, "AR 시작", Toast.LENGTH_SHORT).show();
                int modelResourceId = R.raw.rounded_chair;
                Uri modelUri = Uri.parse("android.resource://" + getPackageName() + "/raw/" + modelResourceId);

                arFragment.setModelUri(modelUri);

            } catch (CameraNotAvailableException e) {
                // AR 카메라 사용 불가능한 예외 처리
                Toast.makeText(this, "AR 카메라를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void performAIRecommendation(ImageData imageData) {
        // AI 추천 로직을 수행하는 메서드
        // 장소를 파악하고 가구 리스트를 가져와서 보여줌
        // 이 부분은 앱의 기획과 구현에 따라 실제로 구현되어야 합니다.

        //api 호출
        // 1. 장소를 식별한다.
        showLoadingIndicator(); // 로딩 인디케이터 표시

        String identifiedPlace = getIdentifyPlace(imageData);
        Log.d("aaaa","identifiedPlace : "+identifiedPlace);

        if (identifiedPlace != null && !identifiedPlace.isEmpty()) {
            // 2. 장소에 맞는 가구 리스트를 가져온다.
            Log.d("aaaa","bbbbb");
            //fetchRecommendedFurniture(identifiedPlace);
        } else {
            Toast.makeText(this, "장소 식별에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }


    }

    private String getIdentifyPlace(ImageData imageData) {
        // 임시 파일 경로 가져오기
        String tempFilePath = imageData.getImageUrl(); //이미지 절대경로 가져오기.
        File imageFile = new File(tempFilePath);

        // 멀티파트로 파일을 보내는 경우 파라미터 생성 방법, (파일명, 파일 타입)
        RequestBody fileBody = RequestBody.create(imageFile, MediaType.parse("image/*"));
        MultipartBody.Part photo = MultipartBody.Part.createFormData("photo", imageFile.getName(), fileBody);


        // 장소를 식별하는 API 호출.
        Call<ApiResponse<Place>> call = api.identifyPlace(photo);
        AtomicReference<String> rtnVal = new AtomicReference<>("");

        call.enqueue(new Callback<ApiResponse<Place>>() {
            @Override
            public void onResponse(Call<ApiResponse<Place>> call, Response<ApiResponse<Place>> response) {
                // API 호출 성공 시 처리 로직
                hideLoadingIndicator(); // 로딩 인디케이터 숨김

                if (response.isSuccessful()) {
                    ApiResponse<Place> apiResponse = response.body();
                    //Log.d("Response", "line 175 response.body(): " + response.body());
                    //Log.d("Response", "line 176 ApiResponse: " + apiResponse.getData());
                    if (apiResponse != null && apiResponse.getData() != null) {
                        // 장소 식별 결과를 사용하여 추가 로직 수행
                        Place identifiedPlace = apiResponse.getData();
                        // 결과값 리턴
                        rtnVal.set(identifiedPlace.getCategory());

                    }
                } else {
                    // API 호출 실패 시 처리 로직
                    Log.e("API", "API 호출 실패: ");
                    hideLoadingIndicator(); // 로딩 인디케이터 숨김
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Place>> call, Throwable t) {
                // API 호출 실패 시 처리 로직
                Log.e("API", "API 호출 실패: " + t.getMessage());
            }
        });
        //------------------------------------------------------------------------
        return rtnVal.get();
        // 테스트 용도
        //return "식당";
    }

    private void fetchRecommendedFurniture(String placeId) {
        Call<ApiResponse<FurnitureList>> call = api.getRecommendedFurniture(placeId);
        call.enqueue(new Callback<ApiResponse<FurnitureList>>() {
            @Override
            public void onResponse(Call<ApiResponse<FurnitureList>> call, Response<ApiResponse<FurnitureList>> response) {
                if (response.isSuccessful()) {
                    ApiResponse<FurnitureList> furnitureApiResponse = response.body();
                    if (furnitureApiResponse != null && furnitureApiResponse.isSuccess()) {
                        FurnitureList furnitureList = furnitureApiResponse.getData();
                        // 추천 가구 리스트를 사용하여 필요한 처리를 수행
                        handleRecommendedFurnitureList(furnitureList);
                    } else {
                        // API 응답이 실패한 경우 처리 로직
                        handleApiFailure();
                    }
                } else {
                    // API 응답이 실패한 경우 처리 로직
                    handleApiFailure();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FurnitureList>> call, Throwable t) {
                // API 호출 실패 시 처리 로직
                handleApiFailure();
            }
        });
    }

    private void showFurnitureList(FurnitureList furnitureList) {
        // 가구 리스트를 화면에 보여주는 로직
        // 이 부분은 앱의 기획과 구현에 따라 실제로 구현되어야 합니다.

        // 테스트용도
        for (Furniture furniture : furnitureList.getFurnitureList()) {
            System.out.println(furniture.getName() + " - " + furniture.getDescription());
        }
        //---------------------- 운영 ---------------------------------------
        // 가구 리스트를 담을 LinearLayout
        LinearLayout furnitureContainer = findViewById(R.id.furnitureContainer);

        // 기존에 표시된 가구들을 모두 제거
        furnitureContainer.removeAllViews();

        // 가구 리스트를 순회하면서 각 가구를 화면에 추가
        for (Furniture furniture : furnitureList.getFurnitureList()) {
            // 리스트 아이템 레이아웃 파일을 인플레이트
            View furnitureItemView = LayoutInflater.from(this).inflate(R.layout.item_furniture, null);

            // 가구 이미지를 표시하는 ImageView
            ImageView furnitureImage = furnitureItemView.findViewById(R.id.furnitureImage);
            // 가구 이미지 설정 (여기서는 placeholder 이미지로 대체)
            furnitureImage.setImageResource(R.drawable.ic_furniture_placeholder);

            // 가구 이름을 표시하는 TextView
            TextView furnitureName = furnitureItemView.findViewById(R.id.furnitureName);
            // 가구 이름 설정
            furnitureName.setText(furniture.getName());

            // 가구 설명을 표시하는 TextView
            TextView furnitureDescription = furnitureItemView.findViewById(R.id.furnitureDescription);
            // 가구 설명 설정
            furnitureDescription.setText(furniture.getDescription());

            // 가구 리스트에 해당 리스트 아이템을 추가
            furnitureContainer.addView(furnitureItemView);
        }
    }

    private void handleRecommendedFurnitureList(FurnitureList furnitureList) {
        // 추천 가구 리스트를 사용하여 필요한 처리를 수행
        if (furnitureList != null) {
            showFurnitureList(furnitureList); // 가구 리스트 보여주기
        } else {
            Toast.makeText(this, "가구 추천에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleApiFailure() {
        // API 호출 실패 시 처리 로직
        Log.e("API", "API 호출 실패: ");
    }

    //------------ START :2023-06-24 -------------------------------------
    // 카메라 권한 요청 코드
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 123;

    // 권한 요청 메서드
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    // 권한 요청 결과 처리 메서드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "카메라 구동  성공 했습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "카메라 구동  실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 카메라 권한이 허용되지 않은 경우, 권한 요청을 실행합니다.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // 카메라 권한이 이미 허용된 경우, 사진 촬영을 실행합니다.
            capturePhoto();
        }
    }
    //------------ END -------------------------------------
    private void showLoadingIndicator() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("");
        progressDialog.setMessage("로딩 중...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideLoadingIndicator() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}

