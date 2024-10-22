<!-- 考生答题界面 -->
<template>
  <el-container direction="horizontal">
    <!-- 左侧-题目索引 -->
    <el-aside width="260px" style="margin-top: 20px; margin-left: 20px">
      <el-card shadow="never" style="margin-bottom: 20px">
        <template #header>
          <span> {{ testForm.testName }} </span>
        </template>
        <el-row>
          <el-col
            :span="8"
            v-for="(item, i) in ['当前', '未答', '已答']"
            :key="i"
          >
            <div style="text-align: center">
              <el-button
                size="small"
                circle
                disabled
                :id="'logo-' + i"
                style="cursor: auto; margin-bottom: 5px"
              ></el-button>
              <span
                style="
                  flex-direction: column;
                  display: flex;
                  align-items: center;
                "
                >{{ item }}</span
              >
            </div>
          </el-col>
        </el-row>
      </el-card>
      <el-card
        shadow="hover"
        style="margin-bottom: 10px"
        v-for="(type, index) in filteredQuestionList"
        :key="index"
      >
        <template #header>
          <span style="font-weight: bold">{{ type.name }}</span>
        </template>
        <el-row
          v-for="(list, i) in type.list"
          :key="i"
          :style="{ marginBottom: i + 1 == list.length ? '' : '5px' }"
        >
          <el-col
            :span="Math.floor(24 / CARD_COL_NUMBER)"
            v-for="(item, j) in list"
            :key="j"
          >
            <div style="text-align: center">
              <el-button
                circle
                size="large"
                :class="{
                  'index-btn-border': isCurrentQuestion(index, i, j),
                  'index-btn-style': isAnswered(index, i, j),
                }"
                style="
                  cursor: auto;
                  margin-bottom: 5px;
                  font-weight: bold;
                  font-size: 16px;
                "
                @click="jumpToQuestion(index, i, j)"
              >
                {{ getQuestionNumber(index, i, j) }}
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-card>
      <div style="display: block; text-align: center">
        <el-button type="primary" @click="finish()">结束考试</el-button>
      </div>
    </el-aside>
    <!-- 右侧-答题区域 -->
    <el-main>
      <div
        style="display: flex; line-height: 50px; margin: 0 20px"
        v-if="testInfoFlag"
      >
        <span> 全卷共{{ testForm.questionTotal }}题 </span>
        <span style="margin-left: auto">
          <count-down
            :key="countDownKey"
            :flag="timekeep"
            :remainTime="remainSecond"
            @returnTakeTime="getTime"
          ></count-down>
        </span>
      </div>
      <div style="padding: 0px 20px" v-if="testPaperFlag">
        <choice-question
          v-if="currentQuestion && currentQuestion.typeId === 1"
          :key="`choice-${questionRefreshKey}-${sequence}`"
          :number="sequence"
          :title="currentQuestion.questionTitle"
          :answers="currentQuestion.answer"
          :choose="reply[sequence - 1]"
          @returnAnswer="getReply"
        ></choice-question>
        <judge-question
          v-else-if="currentQuestion && currentQuestion.typeId === 2"
          :key="`judge-${questionRefreshKey}`"
          :number="sequence"
          :questionId="currentQuestion.questionId"
          :title="currentQuestion.questionTitle"
          :choose="reply[sequence - 1]"
          @returnAnswer="getReply"
        ></judge-question>
        <short-answer-question
          v-else-if="currentQuestion && currentQuestion.typeId === 3"
          :key="`short-answer-${questionRefreshKey}`"
          :number="sequence"
          :questionId="currentQuestion.questionId"
          :title="currentQuestion.questionTitle"
          :content="reply[sequence - 1]"
          @returnAnswer="getReply"
        ></short-answer-question>
        <submit-flag-question
          v-else-if="currentQuestion && currentQuestion.typeId === 4"
          :key="`submit-flag-${questionRefreshKey}`"
          :number="sequence"
          :questionId="currentQuestion.questionId"
          :title="currentQuestion.questionTitle"
          :content="reply[sequence - 1]"
          @returnAnswer="getReply"
        ></submit-flag-question>
        <code-audit-question
          v-else-if="currentQuestion && currentQuestion.typeId === 5"
          :key="`code-audit-${questionRefreshKey}`"
          :number="sequence"
          :questionId="currentQuestion.questionId"
          :title="currentQuestion.questionTitle"
          @returnAnswer="getReply"
        ></code-audit-question>
      </div>
      <div class="operation">
        <ul class="end">
          <li>
            <el-button
              :disabled="sequence == 1"
              @click="this.jumpTo(--this.sequence)"
            >
              <el-icon style="margin-right: 10px"><arrow-left-bold /></el-icon>
              <span>上一题</span>
            </el-button>
          </li>
          <li>
            <el-button
              :disabled="sequence == testForm.questionTotal"
              @click="this.jumpTo(++this.sequence)"
            >
              <span>下一题</span>
              <el-icon style="margin-left: 10px"><arrow-right-bold /></el-icon>
            </el-button>
          </li>
        </ul>
      </div>
      <!-- 添加调试信息显示 -->
      <div>{{ debugInfo }}</div>
    </el-main>
  </el-container>
</template>

<script>
import userToken from "@/services/auth-header";
import { dealSelect } from "@/services/response";
import countDown from "@/components/CountDown.vue";
import choiceQuestion from "@/components/ChoiceQuestion.vue";
import judgeQuestion from "@/components/JudgeQuestion.vue";
import shortAnswerQuestion from "@/components/ShortAnswerQuestion.vue";
import submitFlagQuestion from "@/components/SubmitFlagQuestion.vue";
import codeAuditQuestion from "@/components/CodeAuditQuestion.vue";
import moment from "moment";
export default {
  components: {
    countDown,
    choiceQuestion,
    judgeQuestion,
    shortAnswerQuestion,
    submitFlagQuestion,
    codeAuditQuestion,
  },
  data() {
    return {
      // 常量
      CARD_COL_NUMBER: 4,
      CHOICE_NO: 0, // index = typeId - 1
      JUDGE_NO: 1,
      SHORT_ANSWER_NO: 2,
      SUBMIT_FLAG_NO: 3, // 新增：提交flag题
      CODE_AUDIT_NO: 4,  // 新增：代码审计题

      // 解决axios获取数的异步问题
      testInfoFlag: false,
      testPaperFlag: false,

      // 倒计时
      remainSecond: 0,
      countDownKey: 0,
      takeTime: 0,
      timekeep: false,

      userId: "",
      // 考试信息
      testForm: {
        testId: "",
        testName: "",
        questionTotal: 0,
        questionList: [], // 确保这里初始化为空数组
      },

      sequence: 1, // 全局
      questionRowCol: [0, 0, 0, 0, 0, 0],
      questionRefreshKey: 0, // 给自定义component加上key -> 刷新component
      reply: [],
      currentQuestion: null,
      allQuestions: [],
      debugInfo: '', // 添加这行用于调试
    };
  },
  created() {
    this.userId = this.$storage.getStorageSync("user").id;
    this.testForm.testId = this.$route.params.testId;
    this.loadData();
  },
  methods: {
    loadData() {
      console.log('开始加载数据');
      this.loadTestInfo().then((response) => {
        console.log('测试信息加载完成', this.testForm);
        this.testInfoFlag = true;
        this.countDownKey = new Date();
        this.timekeep = true;
      });
      this.loadTestPaper().then((response) => {
        console.log('试卷加载完成', this.allQuestions);
        this.testPaperFlag = true;
        this.updateCurrentQuestion();
      });
    },
    loadTestInfo() {
      return this.$axios
        .get("/test/findById", {
          headers: { Authorization: userToken() },
          params: { testId: this.testForm.testId },
        })
        .then((response) => {
          let res = dealSelect(response.data);
          if (res) {
            this.testForm = res.test;
            this.remainSecond = res.test.testDuration * 60;
          }
        });
    },
    loadTestPaper() {
      return this.$axios
        .get("/question/findQuestionListByTestId", {
          headers: { Authorization: userToken() },
          params: { testId: this.testForm.testId },
        })
        .then((response) => {
          let res = dealSelect(response.data);
          if (res && Array.isArray(res)) {
            this.allQuestions = res;
            this.testForm.questionTotal = res.length;
            this.updateCurrentQuestion();
            this.testForm.questionList = [
              { name: "选择题", length: 0, list: [] },
              { name: "判断题", length: 0, list: [] },
              { name: "简答题", length: 0, list: [] },
              { name: "提交flag题", length: 0, list: [] },
              { name: "代码审计题", length: 0, list: [] },
            ];
            let quesList = [[], [], [], [], []]; // 扩展到5个数组
            let quesNum = [0, 0, 0, 0, 0]; // 扩展到5个计数器

            res.forEach((item, index) => {
              if (item.typeId >= 1 && item.typeId <= 5) {
                quesList[item.typeId - 1].push(item);
                quesNum[item.typeId - 1]++;
                if (quesNum[item.typeId - 1] % this.CARD_COL_NUMBER == 0) {
                  this.testForm.questionList[item.typeId - 1].list.push(
                    quesList[item.typeId - 1]
                  );
                  quesList[item.typeId - 1] = [];
                }
              } else {
                console.error('Invalid typeId:', item.typeId);
              }

              if (index + 1 == this.testForm.questionTotal) {
                for (let i = 0; i < this.testForm.questionList.length; i++) {
                  if (quesList[i].length > 0) {
                    this.testForm.questionList[i].list.push(quesList[i]);
                  }
                  this.testForm.questionList[i].length = quesNum[i];
                }
              }
            });
          } else {
            console.error('Invalid or empty response data');
            this.testForm.questionList = []; // 确保在错误情况下也设置为空数组
          }
        })
        .catch(error => {
          console.error('Error loading test paper:', error);
          this.testForm.questionList = []; // 确保在错误情况下也设置为空数组
        });
    },

    jumpTo(i) {
      console.log('跳转到题目', i);
      if (i < 1) {
        i = 1;
      } else if (i > this.testForm.questionTotal) {
        i = this.testForm.questionTotal;
      }
      this.sequence = i;
      this.updateCurrentQuestion();
      this.questionRefreshKey = new Date();
    },
    getReply(data) {
      this.reply[this.sequence - 1] = data;
    },

    getTime(data) {
      this.takeTime = data;
    },
    finish() {
      let time = this.takeTime;
      this.timekeep = false;
      this.remainSecond -= time;
      this.countDownKey = new Date();
      let notEmptyReply = 0;
      this.reply.forEach((item) => {
        notEmptyReply += item != undefined;
      });
      let isCommit = false;
      if (notEmptyReply < this.testForm.questionTotal) {
        this.$confirm("你还有题目未完成，是否继续？", "提示", {
          distinguishCancelAndClose: true,
          confirmButtonText: "提交",
          cancelButtonText: "取消",
          type: "warning",
        })
          .then(() => {
            isCommit = true;
            this.commit(time);
          })
          .catch(() => {
            // 点击提交后会先then()再catch()，设置isCommit防止提交后计时仍继续
            if (!isCommit) {
              // 计时继续
              this.countDownKey = new Date();
              this.timekeep = true;
            }
          });
      } else {
        this.commit(time);
      }
    },
    commit(time) {
      let arr = "";
      for (let i = 0; i < this.testForm.questionTotal; i++) {
        arr += (this.reply[i] == undefined ? " " : this.reply[i]) + ",";
      }
      this.$axios
        .post(
          "/testHistory/commit",
          this.$qs.stringify(
            {
              studentId: this.userId,
              testId: this.testForm.testId,
              finishTime: moment().format("YYYY-MM-DD HH:mm:ss"),
              takeTime: time,
              reply: this.reply,
            },
            { indices: false }
          ),
          { headers: { Authorization: userToken() } }
        )
        .then((response) => {
          this.$message({
            type: response.data.success ? "success" : "error",
            message: response.data.message,
          });
          if (response.data.success) {
            this.$router.push("/student/testHistory");
          }
        });
    },
    updateCurrentQuestion() {
      this.currentQuestion = this.allQuestions[this.sequence - 1];
    },
    isCurrentQuestion(typeIndex, rowIndex, colIndex) {
      return this.sequence === this.getQuestionNumber(typeIndex, rowIndex, colIndex);
    },

    isAnswered(typeIndex, rowIndex, colIndex) {
      const questionNumber = this.getQuestionNumber(typeIndex, rowIndex, colIndex) - 1;
      return this.reply[questionNumber] !== undefined;
    },

    getQuestionNumber(typeIndex, rowIndex, colIndex) {
      let number = 1;
      for (let i = 0; i < typeIndex; i++) {
        number += this.testForm.questionList[i].length;
      }
      number += rowIndex * this.CARD_COL_NUMBER + colIndex;
      return number;
    },

    jumpToQuestion(typeIndex, rowIndex, colIndex) {
      const questionNumber = this.getQuestionNumber(typeIndex, rowIndex, colIndex);
      this.jumpTo(questionNumber);
    },
  },
  computed: {
    filteredQuestionList() {
      // 添加防护检查
      return (this.testForm.questionList || []).filter(type => type && type.length > 0);
    }
  },
};
</script>

<style scoped>
.el-card {
  height: fit-content;
  text-align: center;
}
/* 左侧索引指示信息 */
#logo-0 {
  border: 2px solid red;
}
#logo-1 {
  border: 1px solid #eee;
}
#logo-2 {
  background-color: #38d39f;
}
/* 题目是否已答背景 */
.index-btn-style {
  background-color: #38d39f !important;
  color: #fff;
}
.index-btn-border {
  position: relative;
  border: 2px solid red !important;
}
/* 上下页按钮 */
.operation .end li button {
  border: none;
  font-size: 17px;
  background-color: #fff;
}
.operation .end li {
  margin: 0 75px;
}
.operation .end {
  list-style: none;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>

