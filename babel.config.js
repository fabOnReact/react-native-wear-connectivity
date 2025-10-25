module.exports = {
  // Using generic Babel presets so the test environment does not require the
  // React Native specific preset which isn't available in this container.
  presets: ['@babel/preset-env', '@babel/preset-typescript'],
  plugins: ['@babel/plugin-transform-flow-strip-types'],
};
