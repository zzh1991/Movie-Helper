const webpack = require('webpack');
const path = require("path");
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');

const port = process.env.PORT || 3000;

module.exports = {
  mode: 'production',
  entry: './src/main/script/index.js',
  output: {
    path: path.resolve(__dirname, 'src/main/resources/static'),
    filename: 'built/bundle.[hash].js',
    publicPath: '/',
  },
  // devtool: 'source-map',
  module: {
    rules: [
      {
        test: /\.(js)$/,
        exclude: /node_modules/,
        use: ['babel-loader'],
      },
      {
        test: /\.css$/,
        use: [
          {
            loader: 'style-loader',
          },
          {
            loader: 'css-loader',
          },
        ],
      },
      {
        test: /\.less$/,
        use: [
          {
            loader: 'style-loader',
          },
          {
            loader: 'css-loader',
          },
          {
            loader: 'less-loader',
            options: {
              javascriptEnabled: true,
            },
          },
        ],
      },
      {
        test: /\.(png|jpg|gif)$/,
        use: [{
          loader: 'url-loader',
          options: {
            limit: 8192,
            outputPath: './src/main/resources/static/images/',
          },
        }],
      },
    ],
  },
  // optimization: {
  //   splitChunks: {
  //     cacheGroups: {
  //       vendor: {
  //         chunks: 'initial',
  //         test: 'vendor',
  //         name: 'vendor',
  //         enforce: true
  //       }
  //     }
  //   }
  // },
  plugins: [
    new HtmlWebpackPlugin({
      template: './src/main/resources/html-template/index.html',
      filename: '../templates/index.html',
    }),
    new CleanWebpackPlugin('./src/main/resources/static/built/*.*', {})],
  devServer: {
    host: '0.0.0.0',
    port: port,
    historyApiFallback: true,
    open: true,
  },
};
