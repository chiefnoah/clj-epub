{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    utils.url = "github:numtide/flake-utils";
    garden-cli.url = "github:nextjournal/garden-cli";
  };

  outputs = { self, nixpkgs, utils, garden-cli }:
    utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
        garden = garden-cli.packages.${system}.garden;
      in
      {
        devShell = with pkgs; mkShell {
          buildInputs = [
            clojure
            clojure-lsp
            cljstyle
            clj-kondo
            babashka
            garden
          ];
          RUST_SRC_PATH = rustPlatform.rustLibSrc;
        };
      }
    );
}
