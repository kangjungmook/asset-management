<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: [Number, String], default: null },
  options: { type: Array, required: true }, // [{ value, label, sublabel }]
  placeholder: { type: String, default: '검색 또는 선택하세요' },
})

const emit = defineEmits(['update:modelValue'])

const root = ref(null)
const inputEl = ref(null)
const open = ref(false)
const query = ref('')
const dirty = ref(false)
const highlightedIndex = ref(-1)

const selectedOption = computed(() => props.options.find((o) => o.value === props.modelValue) || null)

const filteredOptions = computed(() => {
  const q = (dirty.value ? query.value : '').trim().toLowerCase()
  if (!q) return props.options
  return props.options.filter((o) => {
    const haystack = `${o.label} ${o.sublabel || ''}`.toLowerCase()
    return haystack.includes(q)
  })
})

function displayText() {
  return open.value ? query.value : selectedOption.value?.label || ''
}

function openDropdown() {
  open.value = true
  dirty.value = false
  query.value = selectedOption.value?.label || ''
  highlightedIndex.value = filteredOptions.value.findIndex((o) => o.value === props.modelValue)
  nextTick(() => inputEl.value?.select())
}

function closeDropdown() {
  open.value = false
  query.value = ''
  dirty.value = false
}

function selectOption(option) {
  emit('update:modelValue', option.value)
  closeDropdown()
}

function handleOutsideClick(e) {
  if (root.value && !root.value.contains(e.target)) {
    closeDropdown()
  }
}

function moveHighlight(delta) {
  if (!open.value) {
    openDropdown()
    return
  }
  const len = filteredOptions.value.length
  if (len === 0) return
  highlightedIndex.value = (highlightedIndex.value + delta + len) % len
}

function selectHighlighted() {
  const option = filteredOptions.value[highlightedIndex.value]
  if (option) selectOption(option)
}

watch(query, () => {
  if (!dirty.value) return
  highlightedIndex.value = filteredOptions.value.length > 0 ? 0 : -1
})

onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', handleOutsideClick)
})
</script>

<template>
  <div class="searchable-select" ref="root">
    <input
      ref="inputEl"
      class="input"
      type="text"
      :value="displayText()"
      :placeholder="placeholder"
      autocomplete="off"
      @focus="openDropdown"
      @input="query = $event.target.value; dirty = true"
      @keydown.down.prevent="moveHighlight(1)"
      @keydown.up.prevent="moveHighlight(-1)"
      @keydown.enter.prevent="selectHighlighted"
      @keydown.esc="closeDropdown"
    />
    <div v-if="open" class="searchable-select__panel">
      <button
        v-for="(option, idx) in filteredOptions"
        :key="option.value"
        type="button"
        class="searchable-select__option"
        :class="{ 'searchable-select__option--active': idx === highlightedIndex, 'searchable-select__option--selected': option.value === modelValue }"
        @mouseenter="highlightedIndex = idx"
        @click="selectOption(option)"
      >
        <span>{{ option.label }}</span>
        <span v-if="option.sublabel" class="searchable-select__sublabel">{{ option.sublabel }}</span>
      </button>
      <p v-if="filteredOptions.length === 0" class="searchable-select__empty">검색 결과가 없습니다.</p>
    </div>
  </div>
</template>
