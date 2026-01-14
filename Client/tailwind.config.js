
/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: 'class', // Enable class-based dark mode
  presets: [require('@spartan-ng/ui-core/hlm-tailwind-preset')],
  content: [
    './src/**/*.{html,ts}',
    './src/spartan-components/**/*.{html,ts}',
  ],
  theme: {
    extend: {},
  },
  plugins: [],
};
