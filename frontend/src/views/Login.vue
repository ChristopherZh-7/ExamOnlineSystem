<template>
  <div>
    <img src="../assets/login/user_login_bg.png" alt="" class="wave" />
    <div class="container">
      <div class="img"></div>
      <div class="login-box">
        <form action="#" style="width: 360px">
          <img
            src="../assets/login/user_login_icon.png"
            alt=""
            class="avatar"
          />
          <h2 style="letter-spacing: 10px">登录系统</h2>
          <div class="radio-group">
            <div>
              <el-button
                icon="checked"
                round
                size="large"
                :type="isTch"
                @click="changeIdentity(2)"
                >管理</el-button
              >
            </div>
            <div>
              <el-button
                icon="briefcase"
                round
                size="large"
                :type="isStu"
                @click="changeIdentity(3)"
                >员工</el-button
              >
            </div>
          </div>
          <div class="input-group">
            <div class="icon">
              <i class="fa fa-user"></i>
            </div>
            <div>
              <input type="text" class="input" v-model="user.account" />
            </div>
          </div>
          <div class="input-group">
            <div class="icon">
              <i class="fa fa-lock"></i>
            </div>
            <div>
              <input class="input" type="password" v-model="user.password" />
            </div>
          </div>
          <input type="button" class="btn" value="登录" @click="login()" />
        </form>
      </div>
    </div>
  </div>
</template>
<script>
import User from "@/services/user";
export default {
  data() {
    return {
      user: new User("", "", "", "", ""),
      isTch: "",
      isStu: "",
    };
  },
  created() {
    if (this.$storage.getStorageSync("user")) {
      this.$router.push("/home");
    }
  },
  methods: {
    changeIdentity(id) {
      this.user.roleId = id;
      this.isTch = id == 2 ? "primary" : "";
      this.isStu = id == 3 ? "primary" : "";
    },

    isEmptyFields() {
      if (this.user.roleId == "") {
        this.$message.warning("请选择身份");
      } else if (this.user.account == "") {
        this.$message.warning("用户名不能为空");
      } else if (this.user.password == "") {
        this.$message.warning("密码不能为空");
      } else {
        return true;
      }
      return false;
    },
    login() {
      if (this.isEmptyFields()) {
        this.$axios
          .post("/user/login", {
            roleId: this.user.roleId,
            account: this.user.account,
            password: this.user.password,
          })
          .then((response) => {
            if (response.data.token) {
              this.$storage.setStorageSync("user", response.data, 10800000);
              this.$router.push("/home");
            } else {
              this.$message.error(response.data.message || "登录失败，请重试");
            }
          })
          .catch((error) => {
            console.error("登录错误:", error);
            if (error.response) {
              if (error.response.status === 500) {
                this.$message.error("服务器内部错误，请联系管理员");
              } else {
                this.$message.error(error.response.data.message || "登录失败，请重试");
              }
            } else {
              this.$message.error("网络错误，请检查您的网络连接");
            }
          });
      }
    },
  },
};
</script>

<style scoped>
@import url("../assets/login/user_login_style.css");
@import url("https://stackpath.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css");
</style>
