<template>
  <l-map
      class=""
      :zoom="zoom"
      :center="center"
      :bounds="bounds"
      @click="click">
    <l-tile-layer :url="url"/>
    <slot></slot>
  </l-map>
</template>

<script>
export default {
  name: "v-map",

  props: {
    center: {
      type: Array,
      default: () => [47.9959, 7.85222],
    },
    bounds: {},
    zoom: {
      type: Number,
      default: 14,
    }
  },

  data() {
    return {
      url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
    }
  },

  methods: {
    click(e) {
      this.$emit("click", e);
    }
  }
}
</script>