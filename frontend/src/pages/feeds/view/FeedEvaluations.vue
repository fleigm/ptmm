<template>
  <div class="my-8 ">
    <div class="text-secondary">Evaluation results</div>

    <div class="flex gap-4 my-4">
      <div v-for="evaluation in evaluations" :key="evaluation.id"
              :style="{background: evaluation.color}"
              class="flex items-center gap-x-4 relative p-2 text-secondary rounded-full">
        <div
             class="px-2 rounded-full text-white inline-block">
          {{ evaluation.feed.name }}
        </div>
        <div class="relative">
          <div class="rounded-full bg-white w-7 h-7 flex items-center justify-center" v-if="evaluation.loading">
            <v-spinner></v-spinner>
          </div>
          <el-button v-else @click="remove(evaluation)" size="mini" circle icon="el-icon-close"></el-button>
        </div>
      </div>
    </div>

    <div class="my-4">
      <el-checkbox v-model="types.tram">Tram</el-checkbox>
      <el-checkbox v-model="types.subway">Subway</el-checkbox>
      <el-checkbox v-model="types.rail">Rail</el-checkbox>
      <el-checkbox v-model="types.bus">Bus</el-checkbox>
      <el-button size="mini" type=""  @click="updateTypeFilter" class="ml-4">apply</el-button>
    </div>

    <v-reports :evaluations="evaluations" :types="typeFilter">
      <template slot-scope="scope">

        <div class="grid grid-cols-2 gap-4">
          <template v-if="scope.reports.length">
            <v-card header="accuracy">
              <v-accuracy-chart class="relative m-2 h-128" :data-sets="scope.accuracyDataSets"
                                :options="scope.chartOptions"></v-accuracy-chart>
            </v-card>
            <v-card class="">
              <div slot="header" class="p-2 border-b">
                <h2>average Frechet Distance <span class="italic">&delta;<sub>aF</sub></span></h2>
              </div>
              <v-histogram class="relative m-2 h-128" :data-sets="scope.avgFrechetDistanceDataSets"
                           :options="scope.chartOptions"></v-histogram>
            </v-card>
            <v-card>
              <div slot="header" class="p-2 border-b">
                <h2>percentage mismatched hop segments <span class="italic">A<sub>N</sub></span></h2>
              </div>
              <v-histogram class="relative m-2 h-128" :data-sets="scope.anDataSets"
                           :options="scope.chartOptions"></v-histogram>
            </v-card>
            <v-card header="percentage length mismatched hop segments">
              <div slot="header" class="p-2 border-b">
                <h2>percentage length mismatched hop segments <span class="italic">A<sub>L</sub></span></h2>
              </div>
              <v-histogram class="relative m-2 h-128" :data-sets="scope.alDataSets"
                           :options="scope.chartOptions"></v-histogram>
            </v-card>
          </template>
          <template v-else>
            <div class="text-xl font-thin text-secondary py-2">Select feeds to show evaluation results</div>
          </template>
        </div>
      </template>
    </v-reports>
  </div>
</template>

<script>

import VReports from "./Reports";
import VAccuracyChart from "./AccuracyChart";

const colors = ['#1E3A8A', '#3B82F6', '#D97706', '#F59E0B', '#991B1B', '#EF4444', '#064E3B', '#059669', '#4F46E5', '#7C3AED'];

export default {
  name: "v-feed-evaluations",

  components: {VAccuracyChart, VReports},

  data() {
    return {
      evaluations: [],
      availableColors: [...colors.reverse()],
      typeFilter: [0, 1, 2, 3],
      types: {
        tram: true,
        subway: true,
        rail: true,
        bus: true,
      }
    }
  },

  methods: {
    add(feed) {
      if (!Object.prototype.hasOwnProperty.call(feed.extensions, 'de.fleigm.transitrouter.feeds.evaluation.Evaluation')) {
        console.log('Generated feed has no evaluation.');
        return;
      }
      if (this.evaluations.find(e => e.id === feed.id)) {
        return;
      }

      this.evaluations.push({
        id: feed.id,
        feed: feed,
        color: this.availableColors.pop(),
        loading: false,
      });
    },

    remove(feed) {
      let index = this.evaluations.findIndex(e => e.id === feed.id);
      if (index >= 0) {
        this.availableColors.push(this.evaluations[index].color);
        this.evaluations.splice(index, 1);
      }
    },

    updateTypeFilter() {
      const filter = [];
      if (this.types.tram) {
        filter.push(0);
      }
      if (this.types.subway) {
        filter.push(1);
      }
      if (this.types.rail) {
        filter.push(2);
      }
      if (this.types.bus) {
        filter.push(3);
      }
      this.typeFilter = filter;
    }
  },

  created() {
    this.$events.$on('FeedEvaluations:show', this.add);
    this.$events.$on('FeedEvaluations:hide', this.remove);
  },

  beforeDestroy() {
    this.$events.$off('FeedEvaluations:show', this.add);
    this.$events.$off('FeedEvaluations:hide', this.remove);
  }


}
</script>

<style scoped>

</style>