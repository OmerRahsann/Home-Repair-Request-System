/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      backgroundColor: {
        'custom-maroon': '#76323f',
        'custom-black' : '#565656',
        'custom-tan': '#C09F80',
        'custom-grain': '#D7CEC7', // You can change 'custom-gray' to a more descriptive class name
      },
    },
  },
  plugins: [],
}