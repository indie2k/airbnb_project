<template>

<v-data-table
    :headers="headers"
    :items="roomView"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'RoomView',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "id", value: "id" },
        ],
        roomView : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/roomviews')

      this.roomView = temp.data._embedded.roomviews;

    },
    methods: {
    }
  }
</script>

