/*-
 * #%L
 * Zoomist Add-on
 * %%
 * Copyright (C) 2023 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import { css, LitElement, html } from "lit";
import { query, property } from "lit/decorators.js";
import Zoomist from "zoomist";
import zoomistStyles from "zoomist/dist/zoomist.min.css?inline";

export class FcZoomist extends LitElement {
  @query("#container")
  private _zoomistElement!: HTMLElement;
  private _zoomRatio;

  static styles = [
    zoomistStyles,
    css`
      :host {
      }
    `,
  ];

  static properties = {
    src: {
      type: String,
      reflect: true,
    },
    fill: String,
    draggable: Boolean,
    wheelable: Boolean,
    pinchable: Boolean,
    bounds: Boolean,
    maxRatio: {},
    height: {},
    slider: {},
    zoomer: {},
  };

  render() {
    return html` <div id="container" data-zoomist-src="${this.src}"></div>`;
  }

  constructor() {
    super();

    this.fill = "cover";
    this.draggable = true;
    this.wheelable = true;
    this.pinchable = true;
    this.bounds = true;
    this._zoomRatio = 0.1;
    this.maxRatio = false;
    this.height = "auto";
    this.slider = true;
    this.zoomer = true;
  }

  firstUpdated() {
    super.firstUpdated();
    this._zoomist = new Zoomist(this._zoomistElement); 
    this._zoomist.options.slider = this.slider;
    this._zoomist.options.zoomer = this.zoomer;
    this._zoomist.options.bounds = this.bounds;
    this._zoomist.options.fill = this.fill;
    this._zoomist.options.draggable = this.draggable;
    this._zoomist.options.wheelable = this.wheelable;
    this._zoomist.options.pinchable = this.pinchable;
    this._zoomist.options.zoomRatio = this._zoomRatio;
    this._zoomist.options.maxRatio = this.maxRatio;
    this._zoomist.options.height = this.height;
    this.__registerEventListeners();
  }

  willUpdate(changedProperties) {
    if (this._zoomist) {
      if (changedProperties.has("fill")) {
        this._zoomist.options.fill = this.fill;
      }
      if (changedProperties.has("draggable")) {
        this._zoomist.options.draggable = this.draggable;
      }
      if (changedProperties.has("wheelable")) {
        this._zoomist.options.wheelable = this.wheelable;
      }
      if (changedProperties.has("pinchable")) {
        this._zoomist.options.pinchable = this.pinchable;
      }
      if (changedProperties.has("bounds")) {
        this._zoomist.options.bounds = this.bounds;
      }
      if (changedProperties.has("zoomRatio")) {
        this._zoomist.zoomTo(this._zoomRatio);
      }
      if (changedProperties.has("maxRatio")) {
        this._zoomist.options.maxRatio = this.maxRatio;
      }
      if (changedProperties.has("height")) {
        this._zoomist.options.height = this.height;
      }
      if (changedProperties.has("slider")) {
        this._zoomist.options.slider = this.slider;
        this._zoomist.update();
      }
      if (changedProperties.has("zoomer")) {
        this._zoomist.options.zoomer = this.zoomer;
        this._zoomist.update();
      }
    }
  }

  zoom(ratio) {
    this._zoomist.zoom(ratio);
  }

  move(x, y) {
    this._zoomist.move(x, y);
  }

  moveTo(x, y) {
    this._zoomist.moveTo(x, y);
  }

  slideTo(value) {
    this._zoomist.slideTo(value);
  }

  reset() {
    this._zoomist.reset();
  }

  set zoomRatio(val) {
    let oldVal = this.zoomRatio;
    this._zoomRatio = val;
    this.requestUpdate("zoomRatio", oldVal);
  }

  @property()
  get zoomRatio() {
    return this._zoomRatio;
  }

  get containerData() {
    if (this._zoomist) {
      return this._zoomist.getContainerData();
    }
    return { width: 0, height: 0, aspectRatio: 0 };
  }

  __registerEventListeners() {
    this._zoomist.on("ready", () => {
      this.dispatchEvent(new CustomEvent("zoomist-ready"));
    });
    this._zoomist.on("zoom", (ratio) => {
      this.dispatchEvent(
        new CustomEvent("zoomist-zoom", { detail: { ratio: ratio } })
      );
    });
    this._zoomist.on("wheel", (event) => {
      this.dispatchEvent(new CustomEvent("zoomist-wheel"));
    });
    this._zoomist.on("dragStart", (transform, event) => {
      this.dispatchEvent(
        new CustomEvent("zoomist-drag-start", {
          detail: { offsetX: transform.x, offsetY: transform.y },
        })
      );
    });
    this._zoomist.on("drag", (transform, event) => {
      this.dispatchEvent(
        new CustomEvent("zoomist-drag", {
          detail: { offsetX: transform.x, offsetY: transform.y },
        })
      );
    });
    this._zoomist.on("dragEnd", (transform, event) => {
      this.dispatchEvent(
        new CustomEvent("zoomist-drag-end", {
          detail: { offsetX: transform.x, offsetY: transform.y },
        })
      );
    });
    this._zoomist.on("slideStart", (value, event) => {
      this.dispatchEvent(
        new CustomEvent("zoomist-slide-start", { detail: { value: value } })
      );
    });
    this._zoomist.on("slide", (value, event) => {
      this.dispatchEvent(
        new CustomEvent("zoomist-slide", { detail: { value: value } })
      );
    });
    this._zoomist.on("slideEnd", (value, event) => {
      this.dispatchEvent(
        new CustomEvent("zoomist-slide-end", { detail: { value: value } })
      );
    });
    this._zoomist.on("pinchStart", (value, event) => {
      this.dispatchEvent(new CustomEvent("zoomist-pinch-start"));
    });
    this._zoomist.on("pinch", (event) => {
      this.dispatchEvent(new CustomEvent("zoomist-pinch"));
    });
    this._zoomist.on("pinchEnd", (event) => {
      this.dispatchEvent(new CustomEvent("zoomist-pinch-end"));
    });
    this._zoomist.on("resize", (event) => {
      this.dispatchEvent(new CustomEvent("zoomist-resize"));
    });
    this._zoomist.on("reset", () => {
      this.dispatchEvent(new CustomEvent("zoomist-reset"));
    });
    this._zoomist.on("destroy", () => {
      this.dispatchEvent(new CustomEvent("zoomist-destroy"));
    });
    this._zoomist.on("update", () => {
      this.dispatchEvent(new CustomEvent("zoomist-update"));
    });
  }
}

customElements.define("fc-zoomist", FcZoomist);
