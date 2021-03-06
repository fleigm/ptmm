<template>
  <div class="container pb-8">
    <div class="flex justify-between items-center">
      <el-breadcrumb separator-class="el-icon-arrow-right" class="my-8">
        <el-breadcrumb-item :to="{ name: 'evaluation.index' }">Evaluations</el-breadcrumb-item>
        <el-breadcrumb-item>{{ info.name }}</el-breadcrumb-item>
      </el-breadcrumb>
      <div v-if="!notFound && !loading">
        <el-dropdown>
          <el-button size="mini" :disabled="pending">
            Download<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item>
              <el-link :underline="false" :href="downloadLinkGeneratedFeed">Generated GTFS Feed</el-link>
            </el-dropdown-item>
            <el-dropdown-item>
              <el-link :underline="false" :href="downloadLinkFull">All Files</el-link>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>

        <el-popconfirm
            cancel-button-text='No, Thanks'
            confirm-button-text='Yes'
            title="Are you sure to delete this evaluation?"
            @confirm="deleteEvaluation()"
        >
          <el-button slot="reference" plain size="mini" type="danger" :disabled="pending">Delete</el-button>
        </el-popconfirm>

      </div>
    </div>

    <div v-if="loading" v-loading="true" class="w-full h-128"></div>

    <v-finished-view v-if="finished" :feed="info"></v-finished-view>
    <v-failed-view v-else-if="failed" :info="info"></v-failed-view>
    <div v-else class="text-secondary font-light text-2xl">
      Running generation process. This might take a while...
    </div>

    <div v-if="notFound">
      <div class="text-secondary text-2xl font-thin text-center py-8">Could not find evaluation with id {{ id }}</div>
    </div>

  </div>
</template>

<script>
import VFinishedView from "./FinishedView";
import VFailedView from "./FailedView";
import GeneratedFeedService from "../GeneratedFeedService";

export default {
  name: "Index",

  components: {VFailedView, VFinishedView},

  data() {
    return {
      loading: false,
      info: {
        name: '',
      },
      notFound: false,
    }
  },

  computed: {
    id() {
      return this.$route.params.name
    },

    finished() {
      return this.info && this.info.status === 'FINISHED';
    },

    failed() {
      return this.info && this.info.status === 'FAILED';
    },

    pending() {
      return this.info && this.info.status === 'PENDING';
    },

    downloadLinkFull() {
      return `${process.env.VUE_APP_BACKEND_ENDPOINT}/feeds/${this.id}/download`;
    },
    downloadLinkGeneratedFeed() {
      return `${process.env.VUE_APP_BACKEND_ENDPOINT}/feeds/${this.id}/download/generated`;
    }
  },

  methods: {
    fetchInfo() {
      this.loading = true;
      this.$http.get(`feeds/${this.id}`)
          .then(({data}) => {
            this.info = data;
          })
          .catch(({response}) => {
            console.log(response);

            if (response.status === 404) {
              this.notFound = true;
            }
          })
          .finally(() => {
            this.loading = false;
          })
    },

    deleteEvaluation() {
      GeneratedFeedService.deleteEvaluation(this.id)
          .then(() => this.$router.push({name: 'evaluation.index'}))
    },

    download() {
      this.$http.get(`feeds/${this.id}/download`);
    }
  },

  mounted() {
    this.fetchInfo();
  }

}
</script>