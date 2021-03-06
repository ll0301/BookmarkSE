package com.jroomstudio.smartbookmarkeditor.popup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jroomstudio.smartbookmarkeditor.R;
import com.jroomstudio.smartbookmarkeditor.databinding.EditAddItemPopupFragBinding;

import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkNotNull;

public class EditAddItemPopupFragment extends Fragment {


    //public static final String ARGUMENT_EDIT_TASK_ID = "EDIT_TASK_ID";

    // 뷰모델
    private EditAddItemPopupViewModel mViewModel;

    // 프레그먼트 데이터바인딩
    private EditAddItemPopupFragBinding mDataBinding;

    //프래그먼트 인스턴스 생성
    static EditAddItemPopupFragment newInstance() { return new EditAddItemPopupFragment(); }

    // 비어있는 생성자
    public EditAddItemPopupFragment() {}

    // 프래그먼트의 뷰모델 셋팅
    void setViewModel(EditAddItemPopupViewModel viewModel){
        mViewModel = checkNotNull(viewModel);
    }


    // 카테고리리스트
    private ArrayList<String> mCategoryList = new ArrayList<>();
    private int mCategoryCount;
    // set 스피너 어댑터
    void setSpinnerList(ArrayList<String> categoryList, int categoryCount){
        mCategoryList = categoryList;
        mCategoryCount = categoryCount;
    }

    // 아이템 추가할때를 구분하기 위해
    private boolean mIsAdd = false;
    void setViewType(String viewType){
        if(viewType.equals(EditAddItemPopupActivity.ADD_ITEM)){
            mIsAdd = true;
        }
    }


    // 뷰연결
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.edit_add_item_popup_frag,container,false);
        if(mDataBinding == null) {
            mDataBinding = EditAddItemPopupFragBinding.bind(root);
        }
        mDataBinding.setViewmodel(mViewModel);

        // 스피너 리스너 셋팅
        spinnerSelectedListener();


        return mDataBinding.getRoot();
    }

    // 액티비티가 생성될 때
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 라디오그룹 셋팅
        setRadioGroup();
    }

    // 라디오그룹 셋팅
    private void setRadioGroup(){
        // 라디오그룹 체크 리스너
        mDataBinding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId){
                case R.id.rb_bookmarks :
                    // 북마크 선택 시
                    setLinearContainer(LinearLayout.GONE,LinearLayout.VISIBLE);
                    break;
                case R.id.rb_category :
                    // 카테고리 선택 시
                    setLinearContainer(LinearLayout.VISIBLE,LinearLayout.GONE);
                    break;
            }
        });
    }
    // 리니어 컨테이너 셋팅  (카테고리 or 북마크)
    private void setLinearContainer(int categoryAction, int bookmarkAction){
        // 컨테이너 gone or visible 셋팅
        // 카테고리 or 북마크 라디오버튼 선택시
        mDataBinding.contentLinearCategory.setVisibility(categoryAction);
        mDataBinding.contentLinearBookmark.setVisibility(bookmarkAction);
        // 추가 화면일때만 초기화 진행
        if(mIsAdd){
            // Edit text 초기화
            mDataBinding.etCategoryTitle.setText("");
            mDataBinding.etBookmarkTitle.setText("");
            mDataBinding.etBookmarkUrl.setText(R.string.https);
        }

    }

    // 스피너 셀렉트 리스너
    private void spinnerSelectedListener(){

        // 카테고리의 아이템 리스트를 뷰모델에 저장
        // categories 변수는 뷰모델에서 관찰중
        mDataBinding.getViewmodel().categories.clear();
        mDataBinding.getViewmodel().categories.addAll(mCategoryList);

        // 북마크 편집이나 추가가 아니면 스피너 셋팅은 하지 않음
       if(!mViewModel.isSelectBookmark.get()){
            return;
        }else{
           // 아이템 추가이면서 카테고리가 아무것도 없을때
           // 스피너 생성하지않고 북마크를 추가할 수 없도록 편집창을 막음
           if(mCategoryList.size()==0){
               Log.e("category list","0");
               mDataBinding.rbBookmarks.setEnabled(false);
               mViewModel.isSelectBookmark.set(false);
               return;
           }
       }

        // 스피너에 셋팅할 ArrayAdapter 생성
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                getActivity(), R.layout.edit_add_item_row_spinner, mCategoryList);
        // 드롭다운 레이아웃 셋팅
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 스피너 어댑터 셋팅
        mDataBinding.spinnerCategory.setAdapter(spinnerAdapter);


        // 선택된 카테고리를 북마크 생성 팝업의 스피너리스트에서 선택한다.
        mDataBinding.spinnerCategory.setSelection(mCategoryCount);
        mDataBinding.getViewmodel().bookmarkCategory.set(
                mDataBinding.spinnerCategory.getSelectedItem().toString());


        mDataBinding.spinnerCategory.
                setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 변화 감지될때마다 현재 북마크 카테고리 업데이트
                // tvBookmarkCategory 택스트뷰는 뷰모델에서 관찰하고있다.
                mDataBinding.getViewmodel().bookmarkCategory.set(
                        mDataBinding.spinnerCategory.getSelectedItem().toString()
                );
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

}
