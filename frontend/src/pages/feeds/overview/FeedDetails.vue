<template>
  <div class="flex gap-x-4 w-full" v-if="hasFeedDetails">
    <div class="w-1/3">
        <v-metric title="Agency" :value="feedDetails.agencies[0].agency_name" size="small"></v-metric>
        <v-metric title="Website" size="mini">
          <a :href="feedDetails.agencies[0].agency_url" target="_blank"><i class="el-icon-link"></i></a>
        </v-metric>
        <v-metric title="Timezone" :value="feedDetails.agencies[0].agency_timezone" size="mini"></v-metric>
        <v-metric title="Language" :value="feedDetails.agencies[0].agency_lang" size="mini"></v-metric>
    </div>
    <div class="w-2/3">
        <el-table :data="routesAndTripsPerType" size="mini" class="p-2">
          <el-table-column prop="type" label="Type"></el-table-column>
          <el-table-column prop="routes" label="Routes"></el-table-column>
          <el-table-column prop="trips" label="Trips"></el-table-column>
        </el-table>
    </div>
  </div>
  <div v-else>
    <div class="w-full text-xl text-secondary font-thin my-8 text-center">Generating feed info...</div>
  </div>
</template>

<script>
import {routeTypeToString} from "../../../filters/Filters";

export default {
  name: "FeedDetails",

  props: {
    preset: {
      type: Object,
      required: true,
    },
  },

  computed: {
    hasFeedDetails() {
      return Object.prototype.hasOwnProperty.call(this.preset.extensions, 'de.fleigm.transitrouter.gtfs.FeedDetails');
    },

    feedDetails() {
      return this.preset.extensions['de.fleigm.transitrouter.gtfs.FeedDetails'];
    },

    routesAndTripsPerType() {
      if (!this.hasFeedDetails) {
        return null;
      }

      var map = [];
      map.push({type: 'Total', routes: this.feedDetails.routes, trips: this.feedDetails.trips})

      for (const [key, value] of Object.entries(this.feedDetails.routesPerType)) {
        map.push({
          type: routeTypeToString(key) + ' (' + key + ')',
          routes: value,
          trips: this.feedDetails.tripsPerType[key]
        });
      }

      return map;

    }
  }
}
</script>

<style scoped>

</style>