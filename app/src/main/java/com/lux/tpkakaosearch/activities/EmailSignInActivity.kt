package com.lux.tpkakaosearch.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.lux.tpkakaosearch.G
import com.lux.tpkakaosearch.R
import com.lux.tpkakaosearch.databinding.ActivityEmailSignInBinding
import com.lux.tpkakaosearch.model.UserAccount

class EmailSignInActivity : AppCompatActivity() {

    val binding:ActivityEmailSignInBinding by lazy { ActivityEmailSignInBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 툴바를 제목줄로 설정
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)

        binding.btnSignin.setOnClickListener { clickSignIn() }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun clickSignIn(){
        var email:String=binding.etEmail.text.toString()
        var password:String = binding.etPassword.text.toString()

        //Firebase Firestore DB에서 이메일 로그인 확인
        val db:FirebaseFirestore = FirebaseFirestore.getInstance()
        db.collection("emailUsers")
            .whereEqualTo("email",email)
            .whereEqualTo("password",password)
            .get().addOnSuccessListener {
                if (it.documents.size>0){
                    // 로그인 성공
                    //firestore DB 에 랜덤한 document 명을 id로 사용
                    val id:String=it.documents[0].id
                    G.userAccount=UserAccount(id,email)

                    // 로그인 성공 했으면
                    val intent:Intent= Intent(this,MainActivity::class.java)

                    // 기존 task 의 모든 액티비티를 제거하고 새로운 task 로 시작!
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                }else{
                    // 로그인 실패
                    AlertDialog.Builder(this).setMessage("이메일과 비밀번호를 다시 확인해주세요.").create().show()
                    binding.etEmail.requestFocus()
                    binding.etEmail.selectAll()
                }
            }
    }
}