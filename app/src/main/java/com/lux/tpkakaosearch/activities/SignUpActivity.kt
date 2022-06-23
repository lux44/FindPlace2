package com.lux.tpkakaosearch.activities

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.lux.tpkakaosearch.R
import com.lux.tpkakaosearch.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    val binding:ActivitySignUpBinding by lazy { ActivitySignUpBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //툴바를 액션바로 대체하기
        setSupportActionBar(binding.toolbar)
        //액션바에 제목글씨가 표시됨
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)

        binding.btnSignup.setOnClickListener { clickSignup() }

    }
    //업버튼 클릭시에 반응하는 콜백
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun clickSignup(){
        // Firebase FireStore DB 에 사용자 정보 저장하기

        var email:String = binding.etEmail.text.toString()
        var password:String = binding.etPassword.text.toString()
        var passwordConfirm:String = binding.etPasswordConfirm.text.toString()

        // 유효성 검사 - 패스워드와 패스워드확인 이 맞는지 검사
        // kotlin에서는 문자열 비교시에 equals()대신에 == 을 사용하는 것을 권장
        if( password != passwordConfirm ){
            AlertDialog.Builder(this).setMessage("패스워드확인에 문제가 있습니다. 다시 확인하여 입력해주시기 바랍니다.").create().show()
            binding.etPasswordConfirm.selectAll()
            return
        }

        //Firebase Firestore DB 관리객체 얻어오기
        val db= FirebaseFirestore.getInstance()

        // 혹시 이미 가입되어 있는 이메일을 사용한다면... 새로 가입을 불허!!
        db.collection("emailUsers")
            .whereEqualTo("email", email)
            .get().addOnSuccessListener {
                //값을 값을 가진 Document가 여러개 일 수도 있기에..
                if( it.documents.size > 0 ){ // 같은 이메일이 이미 있다는 것임
                    AlertDialog.Builder(this).setMessage("중복된 이메일이 있습니다. 다시 확인하여 입력해주시기 바랍니다.").show()
                    binding.etEmail.requestFocus() //포커스가 없으면 selectAll()이 동작하지 않음
                    binding.etEmail.selectAll()
                }else{ // 같은 이메일이 없다는 것임

                    //저장할 값(이메일, 비밀번호)를 저장하기 위해 HashMap 으로 만들기
                    val user: MutableMap<String, String> = mutableMapOf()
                    user.put("email", email)
                    user.put("password", password)

                    // collection명은 "emailUsers"로 지정 [ RDBMS의 테이블명 같은 역할 ]
                    // document 명이 랜덤하게 만들어짐.
                    db.collection("emailUsers").add(user).addOnSuccessListener {
                        AlertDialog.Builder(this)
                            .setMessage("축하합니다.\n회원가입이 완료되었습니다.")
                            .setPositiveButton("확인", object : DialogInterface.OnClickListener{
                                override fun onClick(p0: DialogInterface?, p1: Int) {
                                    finish()
                                }
                            }).create().show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "회원가입에 오류가 발생했습니다.\n다시 시도해 주시기 바랍니다.", Toast.LENGTH_SHORT).show()
                    }

                }

            }.addOnFailureListener {
                Toast.makeText(this, "서버상태가 불안정합니다. 다시 시도해주세요", Toast.LENGTH_SHORT).show()
            }

    }
}