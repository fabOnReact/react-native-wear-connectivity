let presets;
let plugins = [];

try {
  require.resolve('module:@react-native/babel-preset');
  presets = ['module:@react-native/babel-preset'];
} catch (err) {
  presets = [
    ['@babel/preset-env', { targets: { node: 'current' } }],
    '@babel/preset-typescript',
  ];
  plugins = ['@babel/plugin-transform-flow-strip-types'];
}

module.exports = {
  presets,
  plugins,
};
