package com.jroomstudio.smartbookmarkeditor.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.jroomstudio.smartbookmarkeditor.Injection;
import com.jroomstudio.smartbookmarkeditor.R;
import com.jroomstudio.smartbookmarkeditor.ViewModelHolder;
import com.jroomstudio.smartbookmarkeditor.data.bookmark.Bookmark;
import com.jroomstudio.smartbookmarkeditor.data.category.Category;
import com.jroomstudio.smartbookmarkeditor.popup.EditAddItemPopupActivity;
import com.jroomstudio.smartbookmarkeditor.util.ActivityUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ItemNavigator {

    private DrawerLayout mDrawerLayout;

    // 메인 프래그먼트 뷰모델 태그
    public static final String MAIN_VM_TAG = "MAIN_VM_TAG";

    // 메인 프래그먼트 뷰모델
    private MainViewModel mViewModel;

    // 메인 네비게이션 뷰
    private NavigationView mNavigationView;

    // 네비게이션 스위치
    private Switch mThemeSwitch, mNoticeSwitch;

    // 아이템추가 -> 스피너리스트로 전달할 카테고리 리스트 카운트
    private int mSelectCategoryCount;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_act);

        //툴바셋팅
        setupToolbar();

        //navigation drawer 셋팅
        setupNavigationDrawer();

        // 프래그먼트 생성 및 재활용
        MainFragment mainFragment = findOrCreateViewFragment();
        // 프래그먼트의 뷰모델 생성 및 재활용
        mViewModel = findOrCreateViewModel();
        // 네비게이터 셋팅
        mViewModel.setNavigator(this);
        // 프래그먼트와 뷰모델 연결
        mainFragment.setMainViewModel(mViewModel);

    }

    // 프래그먼트 생성 또는 재활용
    @NonNull
    private MainFragment findOrCreateViewFragment() {
        // Main 프래그먼트
        MainFragment mainFragment =
                (MainFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if(mainFragment == null){
            // 프래그먼트 생성
            mainFragment = MainFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    mainFragment, R.id.content_frame);
        }
        return mainFragment;
    }
    // 프래그먼트 뷰모델 생성 또는 재활용
    private MainViewModel findOrCreateViewModel() {
        // ViewModelHolder(UI 없는 Fragment)
        // -> 뷰모델 생성 후 TAG 구분자인 MAIN_VM_TAG 을 입력하여 생성 또는 재활용
        @SuppressWarnings("unchecked")
        ViewModelHolder<MainViewModel> retainedViewModel =
                (ViewModelHolder<MainViewModel>) getSupportFragmentManager()
                .findFragmentByTag(MAIN_VM_TAG);
        // 입력한 tag 의 뷰모델이 존재한다면
        if(retainedViewModel != null && retainedViewModel.getViewModel() != null){
            //getViewModel() 로 가져와 리턴한다.
            return retainedViewModel.getViewModel();
        }else{
            // 입력한 TAG 의 뷰모델이 없다면 뷰모델을 생성하고 ViewModelHolder 에 추가한다.
            // 로컬 데이터소스 생성, 원격데이터소스 생성, 액티비티 context 입력
            MainViewModel viewModel = new MainViewModel(
                    Injection.provideBookmarksRepository(getApplicationContext()),
                    Injection.provideCategoriesRepository(getApplicationContext()),
                    getApplicationContext()
            );
            // ViewModelHolder(UI 없는 Fragment) 생성
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(),
                    ViewModelHolder.createViewModelHolder(viewModel),
                    MAIN_VM_TAG
            );
            return viewModel;
        }
    }

    // 툴바셋팅 메소드
    private void setupToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }
    // 툴바 타이틀 변경하기
    @Override
    public void setToolbarTitle(String title) {
        ActionBar ab = getSupportActionBar();
        ab.setTitle(title);
    }


    // 옵션메뉴 Item 셀렉트
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                // Open navigation drawer
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Navigation Drawer 셋팅 메소드
    private void setupNavigationDrawer(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimary);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        if(mNavigationView != null){
            setupDrawerContent();
            setupNavigationSwitch();
        }
    }

    // Navigation switch checkedChange Listener
    private void setupNavigationSwitch(){
        // 다크테마 switch 리스너 셋팅
        mThemeSwitch = (Switch) mNavigationView.getMenu().findItem(R.id.theme_switch).getActionView().findViewById(R.id.nav_switch);
        mThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, "다크테마 -> "+mThemeSwitch.isChecked(), Toast.LENGTH_SHORT).show();
        });
        // 푸쉬알림 switch 리스너 셋팅
        mNoticeSwitch = (Switch) mNavigationView.getMenu().findItem(R.id.notice_switch).getActionView().findViewById(R.id.nav_switch);
        mNoticeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, "푸쉬알림 -> "+mNoticeSwitch.isChecked(), Toast.LENGTH_SHORT).show();
        });
    }

    // Navigation view 네부 아이템 select listener
    private void setupDrawerContent(){
        mNavigationView.setNavigationItemSelectedListener((MenuItem item) -> {
            switch(item.getItemId()){
                case R.id.home_navigation_menu_item:
                    // 북마크 프래그먼트 구현
                    Toast.makeText(MainActivity.this, "home", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.note_navigation_menu_item:
                    // 북마크 프래그먼트 구현
                    Toast.makeText(MainActivity.this, "note", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.info_navigation_menu_item:
                    // info 액티비티 이동
                    Toast.makeText(MainActivity.this, "info", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            mDrawerLayout.closeDrawers();
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        // 뷰모델의 ItemNavigator null 셋팅
        mViewModel.onActivityDestroyed();
        super.onDestroy();
    }


    // 카테고리 리스트에서 TITLE 만 추출하여 반환
    private ArrayList<String> setupCategoryList(List<Category> categories){
        // 카테고리가 있으면 카테고리 리스트를 보낸다.
        // 현재 선택되어있는 카테고리도 보낸다.
        ArrayList<String> title = new ArrayList<String>();
        if(categories != null){
            for(Category category : categories){
                title.add(category.getTitle());
                if(category.isSelected()){
                    mSelectCategoryCount = categories.indexOf(category);
                }
            }
        }
        return title;
    }

    // 아이템 추가 팝업 띄우기 - 툴바 + 버튼
    @Override
    public void addNewItems(List<Category> categories) {
        Intent intent = new Intent(this, EditAddItemPopupActivity.class);
        // 인텐트 타입
        intent.setType(EditAddItemPopupActivity.ADD_ITEM);
        // 카테고리 TITLE 리스트
        intent.putStringArrayListExtra(EditAddItemPopupActivity.CATEGORY_LIST,
                setupCategoryList(categories));
        intent.putExtra(EditAddItemPopupActivity.SELECT_CATEGORY,mSelectCategoryCount);
        startActivity(intent);
    }

    // 선택한 카테고리 편집 팝업 띄우기
    @Override
    public void editSelectCategory(Category category, List<Category> categories) {
        Intent intent = new Intent(this, EditAddItemPopupActivity.class);
        // 편집할 카테고리 데이터 전달
        // INTENT 타입
        intent.setType(EditAddItemPopupActivity.EDIT_CATEGORY);
        // 카테고리 ID
        intent.putExtra(EditAddItemPopupActivity.ID,category.getId());
        // 카테고리 TITLE
        intent.putExtra(EditAddItemPopupActivity.TITLE,category.getTitle());
        // 카테고리 POSITION
        intent.putExtra(EditAddItemPopupActivity.POSITION,category.getPosition());
        // 카테고리 선택여부
        intent.putExtra(EditAddItemPopupActivity.SELECTED,category.isSelected());
        //startActivityForResult(intent, EditAddItemPopupActivity.REQUEST_CODE);
        // 카테고리 리스트
        intent.putStringArrayListExtra(EditAddItemPopupActivity.CATEGORY_LIST,
                setupCategoryList(categories));
        intent.putExtra(EditAddItemPopupActivity.SELECT_CATEGORY,mSelectCategoryCount);

        startActivity(intent);
    }
    // 선택한 북마크 편집팝업 띄우기
    @Override
    public void editSelectBookmark(Bookmark bookmark, List<Category> categories) {
        Intent intent = new Intent(this, EditAddItemPopupActivity.class);
        // 편집할 북마크 데이터 전달
        //intent.putExtra(EditAddItemPopupActivity.INTENT_TYPE,EditAddItemPopupActivity.EDIT_BOOKMARK);
        // 인텐트 타입
        intent.setType(EditAddItemPopupActivity.EDIT_BOOKMARK);
        // 북마크 ID
        intent.putExtra(EditAddItemPopupActivity.ID,bookmark.getId());
        // 북마크 제목
        intent.putExtra(EditAddItemPopupActivity.TITLE,bookmark.getTitle());
        // 북마크 포지션
        intent.putExtra(EditAddItemPopupActivity.POSITION,bookmark.getPosition());
        // URL
        intent.putExtra(EditAddItemPopupActivity.URL,bookmark.getUrl());
        intent.putExtra(EditAddItemPopupActivity.BOOKMARK_ACTION,bookmark.getAction());
        // 파비콘 url
        intent.putExtra(EditAddItemPopupActivity.FAVICON_URL,bookmark.getFaviconUrl());
        // 카테고리 리스트
        intent.putStringArrayListExtra(EditAddItemPopupActivity.CATEGORY_LIST,
                setupCategoryList(categories));
        intent.putExtra(EditAddItemPopupActivity.SELECT_CATEGORY,mSelectCategoryCount);

        startActivity(intent);
    }

}
